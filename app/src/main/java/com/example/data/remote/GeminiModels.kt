package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    val mimeType: String,
    val schema: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

// --- Our custom structured JSON response from Elf Coach ---
@JsonClass(generateAdapter = true)
data class ElfQuestionJson(
    val level: Int,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val hint: String = ""
)

@JsonClass(generateAdapter = true)
data class ElfCoachResponse(
    val feedback: String,
    val isCorrect: Boolean?,
    val explanation: String,
    val nextQuestion: ElfQuestionJson?
)
