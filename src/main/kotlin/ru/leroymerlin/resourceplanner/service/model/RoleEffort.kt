package ru.leroymerlin.resourceplanner.service.model

data class RoleEffort(
    val memberCount: Int,
    val totalHours: Double,
    val effortInPersonDays: Int,
    val daysWithHolidays: Int,
    val estimatedDurationDays: Int
)
