package ru.leroymerlin.resourceplanner.client.model

data class YandexTrackerTask(
    val key: String,
    val summary: String,
    val storyPoints: String?,
    val type: YandexTrackerTaskType,
    val status: YandexTrackerTaskStatus,
    val parent: YandexTrackerTaskParent
)

data class YandexTrackerTaskType(
    val key: String,
    val display: String,
)

data class YandexTrackerTaskStatus(
    val key: String,
    val display: String,
)

data class YandexTrackerTaskParent(
    val self: String,
    val key: String,
    val display: String,
)
