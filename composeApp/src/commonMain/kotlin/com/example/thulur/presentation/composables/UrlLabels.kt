package com.example.thulur.presentation.composables

internal fun extractSourceLabel(url: String): String? {
    val host = url
        .substringAfter("://", url)
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .substringBefore(':')
        .removePrefix("www.")
        .trim()

    return host.takeIf { it.isNotBlank() }
}
