package ru.leroymerlin.resourceplanner.rest.model

data class OverestimatedTasksResponse(
    val taskId: String,
    val taskName: String,
    val overestimatedHours: Double
)
