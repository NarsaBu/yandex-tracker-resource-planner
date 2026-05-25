package ru.leroymerlin.resourceplanner.rest.model

data class ErrorContent(
    val code: Code,
    val message: String,
    val details: List<ErrorDetails>?
)

enum class Code {
    INVALID_REQUEST_STRUCTURE,
    INVALID_REQUEST_PARAMETER,
    ENTITY_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    UNEXPECTED_ERROR,
    DOWNSTREAM_SERVICE_ERROR
}
