package com.example.thulur_api.dtos.articles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RateArticleRequest(
    @SerialName("rating")
    val rating: Int,
)
