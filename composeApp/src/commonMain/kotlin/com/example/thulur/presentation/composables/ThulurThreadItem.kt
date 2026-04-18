package com.example.thulur.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp
import thulur_front.composeapp.generated.resources.Res
import thulur_front.composeapp.generated.resources.ic_book_marked

data class ThulurThreadArticleData(
    val id: String,
    val url: String,
    val variant: ThulurArticleItemVariant,
    val title: String,
    val summary: String?,
    val sourceLabel: String?,
    val dateText: String?,
    val timeText: String?,
    val showDate: Boolean = false,
)

@Composable
fun ThulurThreadItem(
    title: String,
    summary: String?,
    onShowWholeSubjectClick: () -> Unit,
    onToggleArticlesClick: () -> Unit,
    onArticleClick: (ThulurThreadArticleData) -> Unit,
    areArticlesVisible: Boolean,
    articles: List<ThulurThreadArticleData>,
    modifier: Modifier = Modifier,
    leadingLaneWidth: Dp = 0.dp,
    contentStartPadding: Dp = 0.dp,
    articlesLeadingContent: (@Composable () -> Unit)? = null,
) {
    val colors = ThulurTheme.SemanticColors.threadItem
    val typography = ThulurTheme.SemanticTypography
    val articlesRowState = rememberLazyListState()
    val controlSpacing = 20.thulurDp()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = leadingLaneWidth + contentStartPadding)
                .padding(end = 15.thulurDp()),
            verticalArrangement = Arrangement.spacedBy(20.thulurDp()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = title,
                    style = typography.threadItemTitle.copy(color = colors.titleColor),
                )

                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(vertical = 5.thulurDp()),
                    horizontalArrangement = Arrangement.spacedBy(controlSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThulurButton(
                        onClick = onShowWholeSubjectClick,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_book_marked),
                                contentDescription = null,
                                modifier = Modifier.width(30.thulurDp()).height(30.thulurDp()),
                            )
                        },
                        shape = RoundedCornerShape(1000.thulurDp()),
                        stateColorsOverride = colors.showWholeSubjectButton,
                        contentDescription = "Show whole subject",
                        tooltipText = "Show whole subject",
                    )
                    ThulurButton(
                        onClick = onToggleArticlesClick,
                        leadingIcon = {
                            Icon(
                                imageVector = if (areArticlesVisible) {
                                    Icons.Outlined.KeyboardArrowUp
                                } else {
                                    Icons.Outlined.KeyboardArrowDown
                                },
                                contentDescription = null,
                                modifier = Modifier.width(30.thulurDp()).height(30.thulurDp()),
                            )
                        },
                        shape = RoundedCornerShape(1000.thulurDp()),
                        stateColorsOverride = colors.toggleArticlesButton,
                        contentDescription = if (areArticlesVisible) "Hide Articles" else "Show Articles",
                        tooltipText = if (areArticlesVisible) "Hide Articles" else "Show Articles",
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                BasicText(
                    text = summary.orEmpty(),
                    style = typography.threadItemSummary.copy(color = colors.summaryColor),
                )
            }
        }

        if (areArticlesVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.thulurDp()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(leadingLaneWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    articlesLeadingContent?.invoke()
                }

                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .desktopHorizontalWheelScroll(articlesRowState),
                    state = articlesRowState,
                    contentPadding = PaddingValues(0.thulurDp()),
                    horizontalArrangement = Arrangement.spacedBy(15.thulurDp()),
                ) {
                    item () {
                        Spacer(modifier = Modifier.width(controlSpacing))
                    }
                    items(
                        items = articles,
                        key = { article -> article.id },
                    ) { article ->
                        ThulurArticleItem(
                            variant = article.variant,
                            title = article.title,
                            summary = article.summary,
                            sourceLabel = article.sourceLabel,
                            dateText = article.dateText,
                            timeText = article.timeText,
                            showDate = article.showDate,
                            modifier = Modifier.height(340.thulurDp()),
                            onClick = { onArticleClick(article) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThulurThreadItemPreview() {
    val articles = listOf(
        ThulurThreadArticleData(
            id = "default",
            url = "https://example.com/default",
            variant = ThulurArticleItemVariant.Default,
            title = "Default Article",
            summary = "Field-level developments are now moving faster than policy responses in several areas.",
            sourceLabel = "wyborcza.pl",
            dateText = "27.03.2026",
            timeText = "8:40",
        ),
        ThulurThreadArticleData(
            id = "trash",
            url = "https://example.com/trash",
            variant = ThulurArticleItemVariant.Trash,
            title = "Trash Article",
            summary = "Field-level developments are now moving faster than policy responses in several areas.",
            sourceLabel = "wyborcza.pl",
            dateText = "27.03.2026",
            timeText = "8:40",
        ),
        ThulurThreadArticleData(
            id = "important",
            url = "https://example.com/important",
            variant = ThulurArticleItemVariant.Important,
            title = "Important Article",
            summary = "Field-level developments are now moving faster than policy responses in several areas.",
            sourceLabel = "wyborcza.pl",
            dateText = "27.03.2026",
            timeText = "8:40",
        ),
    )

    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            ThulurThreadItem(
                title = "3I/Atlas in Solar System",
                summary = "Mysterious visitor from the deep cosmos is currently tearing through our solar system at a staggering speed.",
                onShowWholeSubjectClick = {},
                onToggleArticlesClick = {},
                onArticleClick = {},
                areArticlesVisible = true,
                articles = articles,
            )
        }
    }
}
