package com.example.thulur.presentation.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.thulur.presentation.theme.ThulurArticleItemVariantSemanticColors
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDefaultShape
import com.example.thulur.presentation.theme.thulurDp

enum class ThulurArticleItemVariant {
    Trash,
    Default,
    Important,
}

@Composable
fun ThulurArticleItem(
    variant: ThulurArticleItemVariant,
    title: String,
    summary: String?,
    sourceLabel: String?,
    dateText: String?,
    timeText: String?,
    showDate: Boolean,
    modifier: Modifier = Modifier,
    imageUrl: String?,
    imageLabel: String = "Image",
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val articleItemColors = ThulurTheme.SemanticColors.articleItem
    val colors = articleItemColors.colorsFor(variant)
    val typography = ThulurTheme.SemanticTypography
    val containerShape = thulurDefaultShape()
    val imageShape = thulurDefaultShape()
    val resolvedImageUrl = imageUrl?.takeIf(String::isNotBlank)
    var imageState by remember(resolvedImageUrl) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val interactionColors = articleItemColors.interaction
    val hoveredOverlayAlpha by animateFloatAsState(
        targetValue = if (isHovered && !isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "thulurArticleItemHoveredOverlayAlpha",
    )
    val pressedOverlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "thulurArticleItemPressedOverlayAlpha",
    )
    val titleStyle = when (variant) {
        ThulurArticleItemVariant.Important -> typography.articleItemImportantTitle
        ThulurArticleItemVariant.Trash,
        ThulurArticleItemVariant.Default -> typography.articleItemTitle
    }
    val summaryStyle = when (variant) {
        ThulurArticleItemVariant.Important -> typography.articleItemImportantSummary
        ThulurArticleItemVariant.Trash,
        ThulurArticleItemVariant.Default -> typography.articleItemSummary
    }

    Box(
        modifier = modifier
            .width(variant.width())
            .fillMaxHeight()
            .clip(containerShape)
            .hoverable(
                interactionSource = interactionSource,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .then(
                if (variant == ThulurArticleItemVariant.Important) {
                    Modifier.border(
                        width = 1.thulurDp(),
                        color = colors.outlineColor,
                        shape = containerShape,
                    )
                } else {
                    Modifier
                }
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(interactionColors.restContainerColor),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    interactionColors.hoveredContainerColor.copy(alpha = hoveredOverlayAlpha),
                ),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    interactionColors.pressedContainerColor.copy(alpha = pressedOverlayAlpha),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.thulurDp()),
            verticalArrangement = Arrangement.spacedBy(15.thulurDp()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.thulurDp())
                    .clip(imageShape)
                    .background(colors.imageContainerColor),
            ) {
                ArticleImagePlaceholder(
                    imageLabel = imageLabel,
                    labelColor = colors.imageLabelColor,
                    modifier = Modifier.fillMaxSize(),
                )

                if (resolvedImageUrl != null) {
                    AsyncImage(
                        model = resolvedImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onState = { state -> imageState = state },
                    )
                }

                if (imageState is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = articleItemColors.imageLoadingIndicatorColor,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.thulurDp()),
                    verticalArrangement = Arrangement.spacedBy(15.thulurDp()),
                ) {
                    BasicText(
                        text = title,
                        style = titleStyle.copy(color = colors.textColor),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = summary.orEmpty(),
                        style = summaryStyle.copy(color = colors.textColor),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                ArticleItemBottomRow(
                    sourceLabel = sourceLabel,
                    dateText = dateText,
                    timeText = timeText,
                    showDate = showDate,
                )
            }
        }
    }
}

@Composable
private fun ArticleImagePlaceholder(
    imageLabel: String,
    labelColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val typography = ThulurTheme.SemanticTypography

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = imageLabel,
            style = typography.articleItemImageLabel.copy(color = labelColor),
        )
    }
}

@Composable
private fun ArticleItemBottomRow(
    sourceLabel: String?,
    dateText: String?,
    timeText: String?,
    showDate: Boolean,
) {
    val hasSource = sourceLabel != null
    val hasDateTime = timeText != null

    when {
        hasSource && hasDateTime -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArticleItemLink(sourceLabel = sourceLabel)
            ThulurDateTime(
                dateText = dateText,
                timeText = timeText,
                showDate = showDate,
            )
        }

        hasSource -> Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArticleItemLink(sourceLabel = sourceLabel)
        }

        hasDateTime -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThulurDateTime(
                dateText = dateText,
                timeText = timeText,
                showDate = showDate,
            )
        }
    }
}

@Composable
private fun ArticleItemLink(sourceLabel: String) {
    val colors = ThulurTheme.SemanticColors.articleItem
    val typography = ThulurTheme.SemanticTypography

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.thulurDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = colors.linkColor,
        )
        BasicText(
            text = sourceLabel,
            style = typography.articleItemLink.copy(color = colors.linkColor),
        )
    }
}

@Composable
@ReadOnlyComposable
private fun ThulurArticleItemVariant.width() = when (this) {
    ThulurArticleItemVariant.Trash -> 250.thulurDp()
    ThulurArticleItemVariant.Default -> 275.thulurDp()
    ThulurArticleItemVariant.Important -> 300.thulurDp()
}

@Composable
@ReadOnlyComposable
private fun com.example.thulur.presentation.theme.ThulurArticleItemSemanticColors.colorsFor(
    variant: ThulurArticleItemVariant,
): ThulurArticleItemVariantSemanticColors = when (variant) {
    ThulurArticleItemVariant.Trash -> trash
    ThulurArticleItemVariant.Default -> default
    ThulurArticleItemVariant.Important -> important
}

@Preview
@Composable
private fun ThulurArticleItemPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            Row(
                modifier = Modifier.height(340.thulurDp()),
                horizontalArrangement = Arrangement.spacedBy(15.thulurDp()),
            ) {
                ThulurArticleItem(
                    variant = ThulurArticleItemVariant.Default,
                    title = "Default Article",
                    summary = "Field-level developments are now moving faster than policy responses in several areas.",
                    sourceLabel = "wyborcza.pl",
                    dateText = "27.03.2026",
                    timeText = "8:40",
                    showDate = false,
                    imageUrl = null,
                )
                ThulurArticleItem(
                    variant = ThulurArticleItemVariant.Important,
                    title = "Important Article",
                    summary = "Field-level developments are now moving faster than policy responses in several areas.",
                    sourceLabel = "wyborcza.pl",
                    dateText = "27.03.2026",
                    timeText = "8:40",
                    showDate = false,
                    imageUrl = "https://example.com/important.jpg",
                )
            }
        }
    }
}
