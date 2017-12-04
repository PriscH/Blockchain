package com.prisch.blockchain

enum class BlockField(val version: Int, val nodeName: String) {

    VERSION                (0, "version"),
    HEIGHT                 (0, "height"),
    TRANSACTIONS           (0, "transactions"),
    NONCE                  (0, "nonce"),
    PREVIOUS_HASH          (0, "previousHash"),
    HASH                   (0, "hash"),
    PROPERTIES             (0, "properties"),
    STOP_JACO              (0, "STOP_JACO");
}