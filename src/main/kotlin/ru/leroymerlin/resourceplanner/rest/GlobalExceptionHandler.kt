package ru.leroymerlin.resourceplanner.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import ru.leroymerlin.resourceplanner.rest.model.Code
import ru.leroymerlin.resourceplanner.rest.model.ErrorContent
import ru.leroymerlin.resourceplanner.rest.model.ErrorResponse

private val logger = KotlinLogging.logger {}

@ResponseBody
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnspecifiedException(exception: Exception): ErrorResponse {
        logger.error(exception) { "Unexpected error" }

        return ErrorResponse(
            ErrorContent(Code.UNEXPECTED_ERROR, exception.message ?: "Unexpected error", emptyList())
        )
    }
}