package com.example.thulur_api.dtos.feeds

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowFeedRequest(
    @SerialName("identifier")
    val identifier: String,
)
