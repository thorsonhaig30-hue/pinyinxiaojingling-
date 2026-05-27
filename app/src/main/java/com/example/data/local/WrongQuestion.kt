package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_questions")
data class WrongQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val level: Int,
    val questionText: String,
    val optionsJson: String = "", // Comma-separated or JSON string for options
    val correctAnswer: String,
    val explanation: String,
    val hint: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
