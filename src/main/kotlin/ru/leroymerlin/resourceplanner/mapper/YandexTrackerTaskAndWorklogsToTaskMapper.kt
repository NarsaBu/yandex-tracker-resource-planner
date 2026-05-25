package ru.leroymerlin.resourceplanner.mapper

import org.springframework.stereotype.Component
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTask
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTaskLink
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTaskWorklog
import ru.leroymerlin.resourceplanner.service.model.Task
import ru.leroymerlin.resourceplanner.service.model.TaskLink

@Component
class YandexTrackerTaskAndWorklogsToTaskMapper {

    fun map(
        yandexTrackerTasks: List<YandexTrackerTask>,
        yandexTrackerTaskWorklogs: Map<String, List<YandexTrackerTaskWorklog>>,
        yandexTrackerTaskLinks: Map<String, List<YandexTrackerTaskLink>>
    ): List<Task> {
        return yandexTrackerTasks.map { task ->
            val durationInHours = yandexTrackerTaskWorklogs[task.key]
                ?.sumOf { parseIso8601DurationToHours(it.duration) }

            val taskLinks = yandexTrackerTaskLinks.get(task.key)
                ?.map { link ->
                    TaskLink(
                        taskId = link.`object`.key,
                        taskName = link.`object`.display,
                        taskLinkType = link.type.id,
                        taskLinkDirection = link.direction
                    )
                }

            Task(
                taskId = task.key,
                taskName = task.summary,
                estimation = task.storyPoints?.toDouble()?.toInt() ?: 0,
                timeSpent = durationInHours ?: 0.0,
                taskType = task.type.key,
                taskStatus = task.status.key,
                parentTaskId = task.parent.key,
                taskLinks = taskLinks ?: emptyList()
            )
        }
    }

    private fun parseIso8601DurationToHours(duration: String): Double {
        require(duration.startsWith("P")) {
            "Invalid ISO 8601 duration format (must start with 'P'): $duration"
        }

        val content = duration.substring(1)
        val (datePart, timePart) = if ("T" in content) {
            content.split("T", limit = 2)
        } else {
            listOf(content, "")
        }

        var totalHours = 0.0

        extractValue(datePart, 'Y')?.let { totalHours += it * 365 * 24 }
        extractValue(datePart, 'M')?.let { totalHours += it * 30 * 24 }
        extractValue(datePart, 'W')?.let { totalHours += it * 7 * 24 }
        extractValue(datePart, 'D')?.let { totalHours += it * 24 }

        extractValue(timePart, 'H')?.let { totalHours += it }
        extractValue(timePart, 'M')?.let { totalHours += it / 60 }
        extractValue(timePart, 'S')?.let { totalHours += it / 3600 }

        return totalHours
    }

    private fun extractValue(part: String, unit: Char): Double? {
        val regex = Regex("""(\d+(?:\.\d+)?)$unit""")
        return regex.find(part)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    }
}
