package ru.leroymerlin.resourceplanner.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.leroymerlin.resourceplanner.rest.model.OverestimatedTasksResponse
import ru.leroymerlin.resourceplanner.service.yandextrackerprocessors.YandexTrackerTasksReceiver

private val logger = KotlinLogging.logger {}

@Service
class OverestimatedTaskSearchService(
    private val yandexTrackerTasksReceiver: YandexTrackerTasksReceiver
) {

    fun search(parentTaskId: String): List<OverestimatedTasksResponse> {
        logger.info { "Starting to search overestimated tasks" }

        return runBlocking(Dispatchers.IO) {
            yandexTrackerTasksReceiver.receive(parentTaskId)
                .asSequence()
                .filter { task -> task.taskStatus == "finished" }
                .mapNotNull { task ->
                    val diff = task.timeSpent - task.estimation * 8

                    if (diff > 16) {
                        OverestimatedTasksResponse(
                            taskId = task.taskId,
                            taskName = task.taskName,
                            overestimatedHours = (task.timeSpent - task.estimation * 8)
                        )
                    } else null
                }
                .toList()
        }
    }
}
