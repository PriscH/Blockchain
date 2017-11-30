package com.prisch.communication

data class PlainMessage(
        val type: ResponseType,
        val content: String
)

enum class ResponseType {
    INFO, WARNING, ERROR
}