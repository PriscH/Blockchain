package com.prisch.util

sealed class Result {

    abstract fun and(check: () -> Boolean, failureMessage: String): Result

    abstract fun and(otherResult: Result): Result

    companion object {
        fun of(check: () -> Boolean, failureMessage: String) =
                if (check()) Success
                else Failure(failureMessage)
    }
}

object Success : Result() {
    override fun and(check: () -> Boolean, failureMessage: String) = Result.of(check, failureMessage)

    override fun and(otherResult: Result) = otherResult
}

data class Failure(val message: String) : Result() {
    override fun and(check: () -> Boolean, failureMessage: String) = this

    override fun and(otherResult: Result) = this
}