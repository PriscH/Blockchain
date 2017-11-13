package com.prisch.blockchain

import com.fasterxml.jackson.databind.JsonNode

data class Message (
        val clientName: String,
        val clientVersion: Int,
        val content: JsonNode
)