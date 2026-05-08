package com.example.thulur.presentation.chat.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ChatMarkdown(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val typography = ThulurTheme.SemanticTypography
    val colors = ThulurTheme.SemanticColors.chatScreen
    val blocks = parseChatMarkdown(
        markdown = markdown,
        bodyStyle = typography.chatMarkdownBody,
        codeStyle = typography.chatMarkdownCode,
        textColor = textColor,
        codeBackground = colors.markdownCodeBackground,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.thulurDp()),
    ) {
        blocks.forEach { block ->
            when (block) {
                is ChatMarkdownBlock.Paragraph -> BasicText(
                    text = block.text,
                    style = typography.chatMarkdownBody.copy(color = textColor),
                )

                is ChatMarkdownBlock.BulletList -> Column(
                    verticalArrangement = Arrangement.spacedBy(4.thulurDp()),
                ) {
                    block.items.forEach { item ->
                        BasicText(
                            text = buildAnnotatedString {
                                append("\u2022 ")
                                append(item)
                            },
                            style = typography.chatMarkdownBody.copy(color = textColor),
                        )
                    }
                }
            }
        }
    }
}

internal sealed interface ChatMarkdownBlock {
    data class Paragraph(val text: AnnotatedString) : ChatMarkdownBlock

    data class BulletList(val items: List<AnnotatedString>) : ChatMarkdownBlock
}

internal fun parseChatMarkdown(
    markdown: String,
    bodyStyle: TextStyle,
    codeStyle: TextStyle,
    textColor: Color,
    codeBackground: Color,
): List<ChatMarkdownBlock> {
    val lines = markdown.replace("\r\n", "\n").split('\n')
    val blocks = mutableListOf<ChatMarkdownBlock>()
    var index = 0

    while (index < lines.size) {
        if (lines[index].isBlank()) {
            index++
            continue
        }

        if (lines[index].isBulletItem()) {
            val items = mutableListOf<AnnotatedString>()
            while (index < lines.size && lines[index].isBulletItem()) {
                items += parseInlineMarkdown(
                    text = lines[index].drop(2),
                    bodyStyle = bodyStyle,
                    codeStyle = codeStyle,
                    textColor = textColor,
                    codeBackground = codeBackground,
                )
                index++
            }
            blocks += ChatMarkdownBlock.BulletList(items)
            continue
        }

        val paragraphLines = mutableListOf<String>()
        while (index < lines.size && lines[index].isNotBlank() && !lines[index].isBulletItem()) {
            paragraphLines += lines[index]
            index++
        }
        blocks += ChatMarkdownBlock.Paragraph(
            text = parseInlineMarkdown(
                text = paragraphLines.joinToString(separator = "\n"),
                bodyStyle = bodyStyle,
                codeStyle = codeStyle,
                textColor = textColor,
                codeBackground = codeBackground,
            ),
        )
    }

    return blocks
}

private fun parseInlineMarkdown(
    text: String,
    bodyStyle: TextStyle,
    codeStyle: TextStyle,
    textColor: Color,
    codeBackground: Color,
): AnnotatedString = buildAnnotatedString {
    appendInlineMarkdown(
        source = text,
        startIndex = 0,
        endIndex = text.length,
        bodyStyle = bodyStyle,
        codeStyle = codeStyle,
        textColor = textColor,
        codeBackground = codeBackground,
    )
}

private fun AnnotatedString.Builder.appendInlineMarkdown(
    source: String,
    startIndex: Int,
    endIndex: Int,
    bodyStyle: TextStyle,
    codeStyle: TextStyle,
    textColor: Color,
    codeBackground: Color,
) {
    var index = startIndex
    while (index < endIndex) {
        when {
            source.startsWith("`", index) -> {
                val closingIndex = source.indexOf('`', startIndex = index + 1)
                if (closingIndex in (index + 1) until endIndex) {
                    withStyle(
                        codeStyle.toCodeSpanStyle().copy(
                            color = textColor,
                            background = codeBackground,
                        ),
                    ) {
                        append(source.substring(index + 1, closingIndex))
                    }
                    index = closingIndex + 1
                } else {
                    append(source[index])
                    index++
                }
            }

            source.startsWith("***", index) -> {
                val closingIndex = source.indexOf("***", startIndex = index + 3)
                if (closingIndex in (index + 3) until endIndex) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        ),
                    ) {
                        appendInlineMarkdown(
                            source = source,
                            startIndex = index + 3,
                            endIndex = closingIndex,
                            bodyStyle = bodyStyle,
                            codeStyle = codeStyle,
                            textColor = textColor,
                            codeBackground = codeBackground,
                        )
                    }
                    index = closingIndex + 3
                } else {
                    append("***")
                    index += 3
                }
            }

            source.startsWith("**", index) -> {
                val closingIndex = source.indexOf("**", startIndex = index + 2)
                if (closingIndex in (index + 2) until endIndex) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendInlineMarkdown(
                            source = source,
                            startIndex = index + 2,
                            endIndex = closingIndex,
                            bodyStyle = bodyStyle,
                            codeStyle = codeStyle,
                            textColor = textColor,
                            codeBackground = codeBackground,
                        )
                    }
                    index = closingIndex + 2
                } else {
                    append("**")
                    index += 2
                }
            }

            source.startsWith("*", index) -> {
                val closingIndex = source.indexOf('*', startIndex = index + 1)
                if (closingIndex in (index + 1) until endIndex) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        appendInlineMarkdown(
                            source = source,
                            startIndex = index + 1,
                            endIndex = closingIndex,
                            bodyStyle = bodyStyle,
                            codeStyle = codeStyle,
                            textColor = textColor,
                            codeBackground = codeBackground,
                        )
                    }
                    index = closingIndex + 1
                } else {
                    append('*')
                    index++
                }
            }

            else -> {
                append(source[index])
                index++
            }
        }
    }
}

private fun String.isBulletItem(): Boolean = startsWith("- ") || startsWith("* ")

private fun TextStyle.toCodeSpanStyle(): SpanStyle = SpanStyle(
    color = color,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontStyle = fontStyle,
    fontSynthesis = fontSynthesis,
    fontFamily = fontFamily,
    fontFeatureSettings = fontFeatureSettings,
    letterSpacing = letterSpacing,
    baselineShift = baselineShift,
    textGeometricTransform = textGeometricTransform,
    localeList = localeList,
    background = background,
    textDecoration = textDecoration,
    shadow = shadow,
)
