package ru.leroymerlin.resourceplanner.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.leroymerlin.resourceplanner.rest.model.Anomaly
import ru.leroymerlin.resourceplanner.rest.model.CalculateDeadlineRequest
import ru.leroymerlin.resourceplanner.rest.model.CalculateDeadlineResponse
import ru.leroymerlin.resourceplanner.service.model.ActiveTask
import ru.leroymerlin.resourceplanner.service.model.PricedTask
import ru.leroymerlin.resourceplanner.service.model.ProjectTimeline
import ru.leroymerlin.resourceplanner.service.model.RoleEffort
import ru.leroymerlin.resourceplanner.service.model.RoleTracker
import ru.leroymerlin.resourceplanner.service.model.ScheduleResult
import ru.leroymerlin.resourceplanner.service.model.Task
import ru.leroymerlin.resourceplanner.service.yandextrackerprocessors.YandexTrackerTasksReceiver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

private val logger = KotlinLogging.logger {}

@Service
class DeadlineCalculateService(
    private val yandexTrackerTasksReceiver: YandexTrackerTasksReceiver
) {

    fun calculate(request: CalculateDeadlineRequest): CalculateDeadlineResponse {
        val rolesMap = request.roles.associate { it.roleName to it.memberCount }
        val tasks = getAllProjectTasks(request.parentTaskId)
            .filter { it.taskStatus != "finished" }

        logger.info { "Starting to calculate deadline" }

        val (pricedTasks, anomalies) = splitForPriceTasksAndAnomalies(tasks)
        val report = calculateScheduleWithRoleBreakdown(pricedTasks, rolesMap)

        return CalculateDeadlineResponse(
            report.projectTimeline,
            roleEfforts = report.roleEfforts,
            anomalies = anomalies
        )
    }

    private fun getAllProjectTasks(parentTaskId: String): List<Task> {
        return runBlocking(Dispatchers.IO) {
            yandexTrackerTasksReceiver.receive(parentTaskId)
        }
    }

    private fun splitForPriceTasksAndAnomalies(tasks: List<Task>): Pair<MutableList<PricedTask>, MutableList<Anomaly>> {
        val taskMap = tasks.associateBy { it.taskId }
        val anomalies = mutableListOf<Anomaly>()
        val pricedTasks = mutableListOf<PricedTask>()

        for (task in tasks) {
            val inwardDependencies = task.taskLinks
                .filter { it.taskLinkType == "depends" && it.taskLinkDirection == "inward" }
                .distinct()

            val price = task.estimation.toDouble() * 8 - task.timeSpent
            val keywords = listOf("[BE]", "[FE]", "[AQA]")

            inwardDependencies.forEach { inward ->
                if (taskMap[inward.taskId] == null) {
                    anomalies += Anomaly(
                        inward.taskId,
                        inward.taskName,
                        "Task is depends from: ${task.taskId}, but this task is not in perimeter of the project"
                    )
                }
            }

            if (task.estimation == 0) {
                anomalies += Anomaly(task.taskId, task.taskName, "The task is without estimation")
                continue
            }

            if (price < 0) {
                anomalies += Anomaly(task.taskId, task.taskName, "The task execution time has expired")
                continue
            }

            if (keywords.all { !task.taskName.contains(it) }) {
                anomalies += Anomaly(task.taskId, task.taskName, "The task implementer is undefined")
                continue
            }

            val relationLink = task.taskLinks
                .firstOrNull { it.taskLinkDirection == "outward" }

            pricedTasks += PricedTask(
                taskId = task.taskId,
                taskName = task.taskName,
                relatedTaskId = relationLink?.taskId,
                relatedTaskName = relationLink?.taskName,
                taskPrice = price
            )
        }

        return pricedTasks to anomalies
    }

    private fun calculateScheduleWithRoleBreakdown(
        pricedTasks: List<PricedTask>,
        rolePool: Map<String, Int>
    ): ScheduleResult {
        if (pricedTasks.isEmpty()) return ScheduleResult(ProjectTimeline(LocalDate.now(), 0, 0), emptyMap())

        val totalTasks = pricedTasks.size

        val completed = mutableSetOf<String>()
        val activeTasks = mutableListOf<ActiveTask>()
        val availableRoles = rolePool.toMutableMap()
        val roleTrackers = mutableMapOf<String, RoleTracker>()

        // Start from the next day
        var currentDate = LocalDate.now().plusDays(1)
        while (currentDate.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            currentDate = currentDate.plusDays(1)
        }
        val projectStartDate = currentDate
        var workingDaysCount = 0
        var projectEndDate: LocalDate? = null

        fun getRequiredRoles(taskName: String): List<String> = when {
            taskName.startsWith("[BE]") -> listOf("BE", "QA")
            taskName.startsWith("[FE]") -> listOf("FE", "QA")
            taskName.startsWith("[AQA]") -> listOf("QA")
            else -> emptyList()
        }

        fun canAssign(required: List<String>): Boolean {
            val needs = required.groupingBy { it }.eachCount()
            return needs.all { (role, count) -> availableRoles.getOrDefault(role, 0) >= count }
        }

        fun assignTask(task: PricedTask, required: List<String>, day: LocalDate) {
            val needs = required.groupingBy { it }.eachCount()
            needs.forEach { (role, count) ->
                availableRoles[role] = availableRoles.getOrDefault(role, 0) - count

                val tracker = roleTrackers.getOrPut(role) {
                    RoleTracker(memberCount = rolePool.getOrDefault(role, 0))
                }
                val coefficient = getRoleCoefficient(task.taskName, role)
                tracker.totalHours += task.taskPrice * count * coefficient

                if (tracker.firstDay == null || day.isBefore(tracker.firstDay!!)) tracker.firstDay = day
                if (tracker.lastDay == null || day.isAfter(tracker.lastDay!!)) tracker.lastDay = day
            }
            activeTasks.add(ActiveTask(task, task.taskPrice, needs.keys.toList()))
        }

        while (completed.size < totalTasks) {
            workingDaysCount++
            projectEndDate = currentDate

            // 1. Implementing tasks (6h/d)
            val finishedToday = mutableListOf<ActiveTask>()
            for (active in activeTasks) {
                active.remainingHours -= 6.0
                if (active.remainingHours <= 0.0) finishedToday.add(active)
            }

            // 2. Complete tasks and free resources
            for (active in finishedToday) {
                completed.add(active.task.taskId)
                active.assignedRoles.forEach { role ->
                    availableRoles.merge(role, 1, Int::plus)
                }
            }
            activeTasks.removeAll(finishedToday)

            // 3. Finish completed tasks
            val readyTasks = pricedTasks.filter { task ->
                !completed.contains(task.taskId) &&
                        activeTasks.none { it.task.taskId == task.taskId } &&
                        (task.relatedTaskId == null || completed.contains(task.relatedTaskId))
            }

            for (task in readyTasks) {
                val required = getRequiredRoles(task.taskName)
                if (canAssign(required)) {
                    assignTask(task, required, currentDate)
                }
            }

            // 4. Goint to the next day
            currentDate = currentDate.plusDays(1)
            while (currentDate.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                currentDate = currentDate.plusDays(1)
            }

            // Deadlock protection
            if (activeTasks.isEmpty() && completed.size < totalTasks) {
                val canContinue = pricedTasks.any { task ->
                    !completed.contains(task.taskId) &&
                            (task.relatedTaskId == null || completed.contains(task.relatedTaskId)) &&
                            canAssign(getRequiredRoles(task.taskName))
                }
                if (!canContinue) {
                    error("Deadlock: не хватает ресурсов или нарушены зависимости.")
                }
            }
        }

        // Project duration calendar
        val calendarDays = if (projectStartDate != null && projectEndDate != null) {
            ChronoUnit.DAYS.between(projectStartDate, projectEndDate).toInt() + 1
        } else 0

        // Report generation
        val roleEfforts = roleTrackers.mapValues { (_, tracker) ->
            val effortInPersonDays = ceil(tracker.totalHours / 6.0).toInt()
            val estimatedDurationDays = ceil(tracker.totalHours / (6.0 * tracker.memberCount)).toInt()
            val daysWithHolidays = if (tracker.firstDay != null && tracker.lastDay != null) {
                ChronoUnit.DAYS.between(tracker.firstDay, tracker.lastDay).toInt() + 1
            } else 0

            RoleEffort(
                memberCount = tracker.memberCount,
                totalHours = tracker.totalHours,
                effortInPersonDays = effortInPersonDays,
                daysWithHolidays = daysWithHolidays,
                estimatedDurationDays = estimatedDurationDays
            )
        }

        val completionDate = LocalDate.now().plusDays(calendarDays.toLong())

        return ScheduleResult(
            projectTimeline = ProjectTimeline(completionDate, workingDaysCount, calendarDays),
            roleEfforts = roleEfforts
        )
    }

    private fun getRoleCoefficient(taskName: String, role: String): Double = when {
        taskName.startsWith("[AQA]") && role == "QA" -> 1.0
        (taskName.startsWith("[BE]") || taskName.startsWith("[FE]")) && role == "QA" -> 0.6
        else -> 1.0
    }
}
