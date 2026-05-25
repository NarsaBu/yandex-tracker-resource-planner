package ru.leroymerlin.resourceplanner.service.model

data class ScheduleResult(
    val projectTimeline: ProjectTimeline,
    val roleEfforts: Map<String, RoleEffort>
)
