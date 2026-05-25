package ru.leroymerlin.resourceplanner.service.yandextrackerprocessors

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.leroymerlin.resourceplanner.mapper.YandexTrackerTaskAndWorklogsToTaskMapper
import ru.leroymerlin.resourceplanner.service.model.Task

private val logger = KotlinLogging.logger {}

@Service
class YandexTrackerTasksReceiver(
    private val subtaskCollectionService: SubtaskCollectionService,
    private val taskWorklogCollectionService: TaskWorklogCollectionService,
    private val taskLinkCollectionService: TaskLinkCollectionService,
    private val yandexTrackerTaskAndWorklogsToTaskMapper: YandexTrackerTaskAndWorklogsToTaskMapper
) {

    suspend fun receive(parentTaskId: String): List<Task> {
        logger.info { "Starting to receive all subtasks from $parentTaskId" }
        val subtasks = subtaskCollectionService.collectSubtasksFrom(parentTaskId)
        logger.info { "All subtasks has been received" }

        val subtasksIds = subtasks.map { it.key }

        logger.info { "Starting to receive subtasks worklogs" }
        val worklogs = taskWorklogCollectionService.collectFrom(subtasksIds)
        logger.info { "All task worklogs has been received" }

        logger.info { "Starting to receive subtasks links" }
        val links = taskLinkCollectionService.collectFrom(subtasksIds)
        logger.info { "All task links has been received" }

        return yandexTrackerTaskAndWorklogsToTaskMapper.map(subtasks, worklogs, links)
    }
}
