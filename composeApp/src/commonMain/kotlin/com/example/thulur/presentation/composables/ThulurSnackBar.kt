package com.example.thulur.presentation.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thulur.presentation.theme.ProvideThulurDesignScale
import com.example.thulur.presentation.theme.ThemeMode
import com.example.thulur.presentation.theme.ThulurDesignScale
import com.example.thulur.presentation.theme.ThulurSnackBarSemanticColors
import com.example.thulur.presentation.theme.ThulurTheme
import com.example.thulur.presentation.theme.rememberThulurSnackBarSemanticColors
import com.example.thulur.presentation.theme.thulurDp
import kotlinx.coroutines.delay

enum class ThulurSnackBarState {
    Default,
    Error,
    Success,
}

private const val SNACK_BAR_AUTO_DISMISS_MILLIS = 5_000L

@Composable
fun ThulurSnackBar(
    message: String,
    modifier: Modifier = Modifier,
    state: ThulurSnackBarState = ThulurSnackBarState.Default,
    colors: ThulurSnackBarSemanticColors = rememberThulurSnackBarSemanticColors(),
    onAction: (() -> Unit)? = null,
    actionText: String? = null,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(SNACK_BAR_AUTO_DISMISS_MILLIS)
        isVisible = false
    }

    val textColor = when (state) {
        ThulurSnackBarState.Error -> colors.errorTextColor
        ThulurSnackBarState.Success -> colors.successTextColor
        ThulurSnackBarState.Default -> colors.defaultTextColor
    }
    val textStyle = ThulurTheme.SemanticTypography.snackBarMessage

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(15.thulurDp()))
                .background(colors.containerColor)
                .padding(horizontal = 10.thulurDp(), vertical = 5.thulurDp()),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = message,
                    style = textStyle.copy(color = textColor),
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onAction != null && actionText != null) {
                        ThulurButton(
                            text = actionText,
                            onClick = onAction,
                            useContainerStates = false,
                            stateColorsOverride = colors.actionButton,
                        )
                    }
                    ThulurButton(
                        leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null) },
                        onClick = { isVisible = false },
                        useContainerStates = false,
                        stateColorsOverride = colors.dismissButton,
                        iconSize = 16.thulurDp()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThulurSnackBarPreview() {
    ProvideThulurDesignScale(scale = ThulurDesignScale()) {
        ThulurTheme(mode = ThemeMode.Light) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(ThulurTheme.Colors.slate.s100)
                    .padding(16.dp),
            ) {
                ThulurSnackBar(
                    message = "This is a default snackbar message",
                    state = ThulurSnackBarState.Default,
                    onAction = {},
                    actionText = "Undo",
                )
                ThulurSnackBar(
                    message = "Something went wrong, please try again",
                    state = ThulurSnackBarState.Error,
                )
                ThulurSnackBar(
                    message = "Changes saved successfully",
                    state = ThulurSnackBarState.Success,
                )
            }
        }
    }
}
