package ru.leroymerlin.resourceplanner.service.yandextrackerprocessors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import ru.leroymerlin.resourceplanner.client.YandexTrackerClient
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTaskWorklog

@Component
class TaskWorklogCollectionService(
    private val client: YandexTrackerClient
) {

    suspend fun collectFrom(taskIds: List<String>, maxParallel: Int = 5): Map<String, List<YandexTrackerTaskWorklog>> {
        val semaphore = Semaphore(permits = maxParallel)

        return coroutineScope {
            taskIds.map { taskId ->
                async {
                    semaphore.withPermit {
                        withContext(Dispatchers.IO) {
                            val worklogs = client.getTaskWorklog(taskId)
                                .collectList()
                                .awaitSingle()

                            taskId to worklogs
                        }
                    }
                }
            }
                .awaitAll()
                .toMap()
        }
    }
}
