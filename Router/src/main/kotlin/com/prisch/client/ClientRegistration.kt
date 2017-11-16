package com.prisch.client

import com.prisch.util.Result

data class ClientRegistration(
        val name: String,
        val version: Int
) {
    fun validate() =
            Result.of({ name.length >= 3 }, "Name should contain at least 3 characters")
                  .and({ version >= 0 }, "The version has to be a positive integer")
}