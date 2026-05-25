package ru.leroymerlin.resourceplanner.client.model

data class YandexTrackerTaskLink(
    val type: YandexTrackerTaskLinkType,
    val direction: String,
    val `object`: YandexTrackerTaskLinkObject
)

data class YandexTrackerTaskLinkType(
    val id: String
)

data class YandexTrackerTaskLinkObject(
    val key: String,
    val display: String
)
