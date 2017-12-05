package com.prisch.util

sealed class Either<L, R> {

    companion object {
        fun <L, R> left(value: L) = Left<L, R>(value)
        fun <L, R> right(value: R) = Right<L, R>(value)
    }
}

data class Left<L, R>(val failure: L) : Either<L, R>()

data class Right<L, R>(val value: R) : Either<L, R>()