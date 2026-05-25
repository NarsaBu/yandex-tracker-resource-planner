package ru.leroymerlin.resourceplanner.service.model

data class PricedTask (
    val taskId: String,
    val taskName: String,
    val relatedTaskId: String?,
    val relatedTaskName: String?,
    val taskPrice: Double,
    val isFinished: Boolean = false
)
