package ru.leroymerlin.resourceplanner.rest

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.leroymerlin.resourceplanner.rest.model.CalculateDeadlineRequest
import ru.leroymerlin.resourceplanner.rest.model.CalculateDeadlineResponse
import ru.leroymerlin.resourceplanner.rest.model.EstimatedTaskRequest
import ru.leroymerlin.resourceplanner.rest.model.OverestimatedTasksResponse
import ru.leroymerlin.resourceplanner.rest.model.UnderestimatedTasksResponse
import ru.leroymerlin.resourceplanner.service.DeadlineCalculateService
import ru.leroymerlin.resourceplanner.service.OverestimatedTaskSearchService
import ru.leroymerlin.resourceplanner.service.UnderestimatedTaskSearchService

const val VERSION = "/v1"

@RestController
@RequestMapping(VERSION)
class TaskController(
    private val underestimatedTaskSearchService: UnderestimatedTaskSearchService,
    private val overestimatedTaskSearchService: OverestimatedTaskSearchService,
    private val deadlineCalculateService: DeadlineCalculateService
) {

    @PostMapping("/tasks:search-underestimate")
    fun searchUnderestimate(@RequestBody request: EstimatedTaskRequest): List<UnderestimatedTasksResponse> {
        return underestimatedTaskSearchService.search(request.parentTaskId)
    }

    @PostMapping("/tasks:search-overestimate")
    fun searchOverestimate(@RequestBody request: EstimatedTaskRequest): List<OverestimatedTasksResponse> {
        return overestimatedTaskSearchService.search(request.parentTaskId)
    }

    @PostMapping("/tasks:calculate-deadline")
    fun calculateDeadline(@RequestBody request: CalculateDeadlineRequest): CalculateDeadlineResponse? {
        return deadlineCalculateService.calculate(request)
    }
}