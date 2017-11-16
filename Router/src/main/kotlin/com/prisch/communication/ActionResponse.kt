package com.prisch.communication

data class ActionResponse(
        val type: ResponseType,
        val message: String
)

enum class ResponseType {
    SUCCESS, ERROR
}