ыpackage com.example.thulur.presentation.dailyfeed.article_reader

import com.example.thulur.domain.model.Article
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.domain.model.ThreadHistory
import com.example.thulur.domain.model.ThreadHistoryDay

internal fun List<DailyFeedThread>.applyCachedReadArticlesToThreads(
    readArticles: Map<String, Boolean>,
): List<DailyFeedThread> {
    if (readArticles.isEmpty()) return this

    var changed = false
    val updatedThreads = map { thread ->
        val updatedArticles = thread.articles.applyCachedReadArticlesToArticles(readArticles)
        if (updatedArticles === thread.articles) {
            thread
        } else {
            changed = true
            thread.copy(articles = updatedArticles)
        }
    }

    return if (changed) updatedThreads else this
}

internal fun ThreadHistory.applyCachedReadArticles(
    readArticles: Map<String, Boolean>,
): ThreadHistory {
    if (readArticles.isEmpty()) return this

    val updatedDays = days.applyCachedReadArticlesToDays(readArticles)
    return if (updatedDays === days) this else copy(days = updatedDays)
}

private fun List<ThreadHistoryDay>.applyCachedReadArticlesToDays(
    readArticles: Map<String, Boolean>,
): List<ThreadHistoryDay> {
    var changed = false
    val updatedDays = map { day ->
        val updatedArticles = day.articles.applyCachedReadArticlesToArticles(readArticles)
        if (updatedArticles === day.articles) {
            day
        } else {
            changed = true
            day.copy(articles = updatedArticles)
        }
    }

    return if (changed) updatedDays else this
}

private fun List<Article>.applyCachedReadArticlesToArticles(
    readArticles: Map<String, Boolean>,
): List<Article> {
    var changed = false
    val updatedArticles = map { article ->
        val updatedArticle = article.applyCachedReadArticles(readArticles)
        if (updatedArticle !== article) {
            changed = true
        }
        updatedArticle
    }

    return if (changed) updatedArticles else this
}

private fun Article.applyCachedReadArticles(
    readArticles: Map<String, Boolean>,
): Article = if (isRead || readArticles[id] != true) {
    this
} else {
    copy(isRead = true)
}
