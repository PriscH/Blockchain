package com.prisch.util

import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Component
class Encryption {

    fun verifySignature(content: String, signedValue: String, publicKeyContent: String): Boolean {
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val signature = Signature.getInstance("SHA1WithRSA")
        signature.initVerify(publicKey)
        signature.update(content.toByteArray())

        return signature.verify(Base64.getDecoder().decode(signedValue))
    }

}