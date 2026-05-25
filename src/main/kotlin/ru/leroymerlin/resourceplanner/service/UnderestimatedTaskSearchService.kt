package ru.leroymerlin.resourceplanner.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.leroymerlin.resourceplanner.rest.model.UnderestimatedTasksResponse
import ru.leroymerlin.resourceplanner.service.yandextrackerprocessors.YandexTrackerTasksReceiver
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

@Service
class UnderestimatedTaskSearchService(
    private val yandexTrackerTasksReceiver: YandexTrackerTasksReceiver
) {

    fun search(parentTaskId: String) : List<UnderestimatedTasksResponse> {
        logger.info { "Starting to search underestimated tasks" }

        return runBlocking(Dispatchers.IO) {
            yandexTrackerTasksReceiver.receive(parentTaskId)
                .asSequence()
                .filter { it.taskStatus != "finished" }
                .mapNotNull { task ->
                    val diff = task.timeSpent - task.estimation * 8

                    if (diff < 0) {
                        UnderestimatedTasksResponse(
                            taskId = task.taskId,
                            taskName = task.taskName,
                            taskStatus = task.taskStatus,
                            underestimatedHours = abs(diff)
                        )
                    } else null
                }
                .sortedBy { task -> task.taskStatus }
                .toList()
        }
    }
}