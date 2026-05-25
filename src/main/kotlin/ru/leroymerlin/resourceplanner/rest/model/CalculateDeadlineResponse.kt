package ru.leroymerlin.resourceplanner.rest.model

import ru.leroymerlin.resourceplanner.service.model.ProjectTimeline
import ru.leroymerlin.resourceplanner.service.model.RoleEffort

data class CalculateDeadlineResponse(
    val projectTimeline: ProjectTimeline,
    val roleEfforts: Map<String, RoleEffort>,
    val anomalies: List<Anomaly>
)

data class Anomaly(
    val taskId: String,
    val taskName: String,
    val anomalyReason: String
)
