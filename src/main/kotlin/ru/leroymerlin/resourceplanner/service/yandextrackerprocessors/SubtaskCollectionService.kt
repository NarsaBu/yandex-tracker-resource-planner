package ru.leroymerlin.resourceplanner.service.yandextrackerprocessors

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import ru.leroymerlin.resourceplanner.client.YandexTrackerClient
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTask
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Service
class SubtaskCollectionService(
    private val client: YandexTrackerClient
) {

    suspend fun collectSubtasksFrom(parentTaskId: String): List<YandexTrackerTask> {
        fun collect(taskId: String): Flux<YandexTrackerTask> = client.searchSubtasks(taskId)
                .concatMap({ task ->
                    if (task.type.key in listOf("userFeature", "techFeature", "story")) {
                        collect(task.key)
                            .delayElements(Duration.ofMillis(300))
                    } else {
                        Flux.just(task)
                    }
                })
                .onErrorResume { ex ->
                    logger.warn(ex) { "Failed to fetch subtasks for $parentTaskId" }
                    Flux.empty()
                }

        return withContext(Dispatchers.IO) {
            collect(parentTaskId)
                .collectList()
                .awaitSingle()
        }
    }
}