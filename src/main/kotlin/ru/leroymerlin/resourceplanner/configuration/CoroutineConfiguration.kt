package ru.leroymerlin.resourceplanner.configuration

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfiguration {

    @Bean
    fun serviceCoroutineScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("subtasks-inject-service"))
}