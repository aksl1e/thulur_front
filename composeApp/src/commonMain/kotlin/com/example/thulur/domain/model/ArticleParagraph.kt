package com.example.thulur.domain.model

/**
 * App-facing paragraph model used by future article reader features.
 */
data class ArticleParagraph(
    val idx: Int,
    val text: String,
    val isNovel: Boolean,
)
