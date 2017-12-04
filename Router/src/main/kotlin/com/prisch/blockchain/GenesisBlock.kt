package com.prisch.blockchain

class GenesisBlock {
    val version = 1
    val height = 1

    val transactions = listOf(GenesisTransaction())

    val nonce = 0

    val previousHash = "0"
    val hash = "GENBLOCK"

    val properties = mapOf<String, String>()
}

class GenesisTransaction {
    val version = 1

    val inputs = listOf(GenesisInput())
    val outputs = listOf(GenesisOutput())

    val feeAmount = 0

    val hash = "GENTRANS"
    val signature = ""
    val publicKey = ""

    val properties = mapOf<String, String>()
}

class GenesisInput {
    val blockHeight = 0
    val transactionHash = "00000000"

    val address = "00000000"
    val amount = 1000000
}

class GenesisOutput {
    val address = "TODOTODO"
    val amount = 1000000
}