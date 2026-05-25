package ru.leroymerlin.resourceplanner.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTask
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTaskLink
import ru.leroymerlin.resourceplanner.client.model.YandexTrackerTaskWorklog
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class YandexTrackerClient(
    private val yandexTrackerWebClient: WebClient
) {

    fun searchSubtasks(parentTaskId: String): Flux<YandexTrackerTask> {
        logger.debug { "POST /v3/issues/_search | parent: $parentTaskId" }

        val requestBody = mapOf("filter" to mapOf("parent" to parentTaskId))

        return yandexTrackerWebClient.post()
            .uri("v3/issues/_search?expand=transitions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(YandexTrackerTask::class.java)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext { task ->
                logger.debug { "Received task: ${task.key} (${task.summary})" }
            }
            .doOnComplete {
                logger.debug { "Search completed for parent: $parentTaskId" }
            }
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
            .onErrorResume { ex ->
                logger.warn(ex) { "Failed to search subtasks for parent $parentTaskId" }
                Flux.empty()
            }
    }

    fun getTaskWorklog(taskId: String): Flux<YandexTrackerTaskWorklog> {
        logger.debug { "GET /v3/issues/$taskId/worklog" }

        return yandexTrackerWebClient.get()
            .uri("v3/issues/$taskId/worklog")
            .retrieve()
            .bodyToFlux(YandexTrackerTaskWorklog::class.java)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext { logger.debug { "Received Worklog for task: $taskId" } }
            .doOnComplete {
                logger.debug { "Worklog fetched for task: $taskId" }
            }
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
            .onErrorResume { ex ->
                logger.warn { "Failed to fetch worklogs $taskId: ${ex.message}" }
                Flux.empty()
            }
    }

    fun getTaskLinks(taskId: String): Flux<YandexTrackerTaskLink> {
        logger.debug { "GET /v3/issues/$taskId/links" }

        return yandexTrackerWebClient.get()
            .uri("v3/issues/$taskId/links")
            .retrieve()
            .bodyToFlux(YandexTrackerTaskLink::class.java)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext { logger.debug { "Received Link for $taskId" } }
            .doOnComplete {
                logger.debug { "Links fetched for task: $taskId" }
            }
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
            .onErrorResume { ex ->
                logger.warn { "Failed to fetch task links $taskId: ${ex.message}" }
                Flux.empty()
            }
    }
}
