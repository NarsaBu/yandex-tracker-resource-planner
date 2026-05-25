package ru.leroymerlin.resourceplanner.rest.model

data class ErrorDetails(
    val message: String,
    val code: String,
    val target: String
)
