package ru.leroymerlin.resourceplanner.rest.model

data class UnderestimatedTasksResponse(
    val taskId: String,
    val taskName: String,
    val taskStatus: String,
    val underestimatedHours: Double
)
