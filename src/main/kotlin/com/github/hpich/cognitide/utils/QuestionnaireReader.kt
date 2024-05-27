package com.github.hpich.cognitide.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class Questionnaire(
    val questionnaireType: String = "",
    val sections: List<Section>,
)

@Serializable
data class Section(
    val title: String,
    val text: String = "",
    val questions: List<Question>,
)

@Serializable
data class Question(
    val title: String,
    val property: String,
    val type: String,
    val answers: List<String>? = null,
    val min: Int? = null,
    val max: Int? = null,
    val step: Int? = null,
)

fun readJSON(filePath: String): Questionnaire {
    val file = File(filePath)
    val jsonString = file.readText()
    val questionnaire = Json.decodeFromString<Questionnaire>(jsonString)
    return questionnaire
}
