package com.prisch.util

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

@Component
class Hasher {

    private val HASH_LENGTH = 8

    fun hash(content: String): String {
        val fullHash = hashWithoutTrunc(content)
        return trunc(fullHash)
    }

    fun hashWithoutTrunc(content: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashStream = digest.digest(content.toByteArray())
            return Base64.getEncoder().encodeToString(hashStream)
        } catch (ex: NoSuchAlgorithmException) {
            throw RuntimeException(ex)
        }

    }

    fun trunc(fullHash: String): String {
        return fullHash.substring(fullHash.length - HASH_LENGTH)
    }

}
