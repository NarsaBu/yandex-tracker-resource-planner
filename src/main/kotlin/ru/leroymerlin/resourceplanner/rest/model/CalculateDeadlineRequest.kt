package ru.leroymerlin.resourceplanner.rest.model

data class CalculateDeadlineRequest(
    val parentTaskId: String,
    val roles: List<Role>
)

data class Role(
    val roleName: String,
    val memberCount: Int
)
