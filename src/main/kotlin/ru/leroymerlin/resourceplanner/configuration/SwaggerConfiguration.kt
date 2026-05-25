package ru.leroymerlin.resourceplanner.configuration

import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

    @Bean
    fun springDocConfiguration(): SpringDocConfiguration {
        return SpringDocConfiguration()
    }

    @Bean
    fun springDocConfigProperties(): SpringDocConfigProperties {
        return SpringDocConfigProperties()
    }

    @Bean
    fun objectMapperProvider(springDocConfigProperties: SpringDocConfigProperties): ObjectMapperProvider {
        return ObjectMapperProvider(springDocConfigProperties)
    }
}
