package ru.leroymerlin.resourceplanner.service.model

data class Task(
    val taskId: String,
    val taskName: String,
    val estimation: Int,
    val timeSpent: Double,
    val taskType: String,
    val taskStatus: String,
    val parentTaskId: String,
    val taskLinks: List<TaskLink>
)

data class TaskLink(
    val taskId: String,
    val taskName: String,
    val taskLinkType: String,
    val taskLinkDirection: String
)