package org.udmurtlang.learn.domain.model

data class Topic(
    val id: String,
    val slug: String,
    val titleUdm: String,
    val titleRu: String,
    val color: Long,
    val emoji: String,
    val sortOrder: Int,
)

data class Word(
    val id: String,
    val topicId: String,
    val udm: String,
    val ru: String,
    val transcription: String,
    val emoji: String,
    val exampleUdm: String? = null,
    val exampleRu: String? = null,
)
