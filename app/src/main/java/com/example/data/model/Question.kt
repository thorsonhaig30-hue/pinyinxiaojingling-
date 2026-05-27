package com.example.data.model

data class Question(
    val id: Int,
    val level: Int,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val hint: String = ""
)
