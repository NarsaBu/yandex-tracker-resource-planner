package ru.leroymerlin.resourceplanner.service.model

import java.time.LocalDate

data class ProjectTimeline(
    val completionDate: LocalDate,
    val workingDays: Int,
    val calendarDays: Int
)
