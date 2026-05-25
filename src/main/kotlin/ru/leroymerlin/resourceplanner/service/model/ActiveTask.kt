package ru.leroymerlin.resourceplanner.service.model

data class ActiveTask(
    val task: PricedTask,
    var remainingHours: Double,
    val assignedRoles: List<String>
)