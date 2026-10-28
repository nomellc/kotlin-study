package com.example.study.core.response

data class ErrorResponse(
    val message: String,
    val errorType: String = "Invalid Argument"
)
