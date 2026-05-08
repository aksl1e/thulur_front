package com.example.thulur.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.theme.ThulurTextFieldSemanticColors
import com.example.thulur.presentation.theme.ThulurTextFieldStateSemanticColors
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurTextFieldSemanticColors
import com.example.thulur.presentation.theme.thulurDefaultShape
import com.example.thulur.presentation.theme.thulurDp

typealias ThulurTextFieldColors = ThulurTextFieldSemanticColors

@Composable
fun ThulurTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    textStyle: TextStyle = ThulurTheme.Typography.bodyLarge,
    placeholderStyle: TextStyle = textStyle,
    stateColorsOverride: ThulurTextFieldStateSemanticColors = ThulurTheme.SemanticColors.authScreen.emailField,
    shape: Shape = thulurDefaultShape(),
    contentPadding: PaddingValues = PaddingValues(start = 15.thulurDp()),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colors = rememberThulurTextFieldSemanticColors(
        enabled = enabled,
        isFocused = isFocused,
        isError = isError,
        stateColorsOverride = stateColorsOverride,
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = colors.containerColor,
        label = "thulurTextFieldContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = colors.contentColor,
        label = "thulurTextFieldContentColor",
    )
    val animatedPlaceholderColor by animateColorAsState(
        targetValue = colors.placeholderColor,
        label = "thulurTextFieldPlaceholderColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = colors.borderColor,
        label = "thulurTextFieldBorderColor",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(shape)
            .background(animatedContainerColor)
            .border(
                width = 0.5.dp,
                color = animatedBorderColor,
                shape = shape,
            ),
        enabled = enabled,
        singleLine = singleLine,
        textStyle = textStyle.copy(color = animatedContentColor),
        cursorBrush = SolidColor(animatedContentColor),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()  // was fillMaxSize()
                    .wrapContentHeight()
                    .padding(contentPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = placeholderStyle.copy(color = animatedPlaceholderColor),
                    )
                }
                innerTextField()
            }
        },
    )
}
