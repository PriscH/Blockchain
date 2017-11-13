package com.prisch.client

import com.prisch.util.Result

data class ClientDetails(
        val name: String,
        val address: String,
        val version: Int,
        val height: Int,
        val lastHash: String
) {
    fun validate() =
            Result.of({ name.length >= 3 }, "Name should contain at least 3 characters")
                  .and({ address.length in 9..15 }, "Address should be an IP4 address")
}