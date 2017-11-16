package com.prisch.client

data class Client(
        val name: String,
        val version: Int
) {
    constructor(clientRegistration: ClientRegistration) : this(
            clientRegistration.name,
            clientRegistration.version
    )
}