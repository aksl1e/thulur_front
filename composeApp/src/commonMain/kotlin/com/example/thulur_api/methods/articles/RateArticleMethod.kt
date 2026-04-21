package com.example.thulur_api.methods.articles

import com.example.thulur_api.config.ThulurApiConfig
import com.example.thulur_api.dtos.articles.RateArticleRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * Encapsulates the `POST /users/me/articles/{article_id}/rate` transport call.
 */
internal class RateArticleMethod(
    private val httpClient: HttpClient,
    private val config: ThulurApiConfig,
) {
    /**
     * Submits a reading-quality rating for the given article.
     *
     * @param articleId Backend article identifier.
     * @param rating Integer score in [0, 10].
     */
    suspend fun execute(
        articleId: String,
        rating: Int,
    ) {
        httpClient.post {
            url("${config.baseUrl}/users/me/articles/$articleId/rate")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RateArticleRequest(rating = rating))
        }
    }
}
