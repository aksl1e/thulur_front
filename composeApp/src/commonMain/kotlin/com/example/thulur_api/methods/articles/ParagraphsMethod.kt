package com.example.thulur_api.methods.articles

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.ParagraphDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * Encapsulates the `/users/me/articles/{article_id}/paragraphs` transport call.
 */
internal class ParagraphsMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    /**
     * Requests paragraph metadata for the given article.
     *
     * @param articleId Backend article identifier.
     * @return Raw backend response as a list of [ParagraphDto].
     */
    suspend fun execute(
        articleId: String,
    ): List<ParagraphDto> = httpClient
        .get {
            url("${config.baseUrl}/users/me/articles/$articleId/paragraphs")
        }
        .body()
}
