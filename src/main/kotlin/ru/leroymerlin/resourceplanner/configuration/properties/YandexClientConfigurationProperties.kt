package ru.leroymerlin.resourceplanner.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.clients.yandex-tracker")
data class YandexClientConfigurationProperties(
    val baseUrl: String,
    val token: String,
    val xOrgId: String
)
