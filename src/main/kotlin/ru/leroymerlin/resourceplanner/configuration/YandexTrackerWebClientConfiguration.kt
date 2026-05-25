package ru.leroymerlin.resourceplanner.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import ru.leroymerlin.resourceplanner.configuration.properties.YandexClientConfigurationProperties

@Configuration
@EnableConfigurationProperties(YandexClientConfigurationProperties::class)
class YandexTrackerWebClientConfiguration(
    private val properties: YandexClientConfigurationProperties
) {

    @Bean
    fun yandexTrackerWebClient(): WebClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.token}")
        .defaultHeader("X-Org-Id", properties.xOrgId)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build()
}
