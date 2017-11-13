package com.prisch.client

import java.time.LocalDateTime

data class ClientSnapshot(
        val clientDetails: ClientDetails,
        val lastRefreshTime: LocalDateTime
)