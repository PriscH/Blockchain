package com.prisch.util

import org.springframework.stereotype.Component

@Component
data class State(
        var version: Int = 1,
        var transactionLimit: Int = 3,
        var hashCheck: String = "++++A"
)