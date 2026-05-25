package ru.leroymerlin.resourceplanner.service.model

import java.time.LocalDate

data class RoleTracker(
    var totalHours: Double = 0.0,
    var firstDay: LocalDate? = null,
    var lastDay: LocalDate? = null,
    val memberCount: Int = 0
)
