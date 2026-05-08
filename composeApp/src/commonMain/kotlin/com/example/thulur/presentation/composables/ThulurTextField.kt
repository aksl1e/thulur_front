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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    onEnterKeyClick: (() -> Unit)? = null,
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
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value))
    }
    val textFieldValue = textFieldValueState.copy(text = value)

    SideEffect {
        if (
            textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValueState = newValue
            if (newValue.text != value) {
                onValueChange(newValue.text)
            }
        },
        modifier = modifier
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || !enabled) {
                    return@onPreviewKeyEvent false
                }

                val isEnterKey = event.key == Key.Enter || event.key == Key.NumPadEnter
                if (!isEnterKey) return@onPreviewKeyEvent false

                if (!singleLine && event.isShiftPressed) {
                    val updatedValue = textFieldValueState.insertAtSelection("\n")
                    textFieldValueState = updatedValue
                    if (updatedValue.text != value) {
                        onValueChange(updatedValue.text)
                    }
                    return@onPreviewKeyEvent true
                }

                if (onEnterKeyClick == null) return@onPreviewKeyEvent false
                onEnterKeyClick()
                true
            }
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

private fun TextFieldValue.insertAtSelection(insertedText: String): TextFieldValue {
    val selectionStart = minOf(selection.start, selection.end)
    val selectionEnd = maxOf(selection.start, selection.end)
    val updatedText = buildString(text.length + insertedText.length) {
        append(text.substring(0, selectionStart))
        append(insertedText)
        append(text.substring(selectionEnd))
    }
    val cursorPosition = selectionStart + insertedText.length

    return copy(
        text = updatedText,
        selection = TextRange(cursorPosition),
        composition = null,
    )
}
