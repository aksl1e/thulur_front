package com.example.thulur_front.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import thulur_front.composeapp.generated.resources.Res
import thulur_front.composeapp.generated.resources.lora_variable
import thulur_front.composeapp.generated.resources.public_sans_variable

@Immutable
data class ThulurTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

@Composable
internal fun rememberThulurTypography(): ThulurTypography {
    val lora = rememberLoraFontFamily()
    val publicSans = rememberPublicSansFontFamily()

    return remember(lora, publicSans) {
        ThulurTypography(
            displayLarge = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 58.sp,
            ),
            displayMedium = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 48.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = lora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 28.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 24.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = publicSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            ),
        )
    }
}

internal fun fallbackThulurTypography(): ThulurTypography {
    val serif = FontFamily.Serif
    val sans = FontFamily.SansSerif

    return ThulurTypography(
        displayLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 58.sp),
        displayMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp),
        displaySmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        headlineSmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
        titleLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 28.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp),
        bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
        labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
    )
}

@Composable
private fun rememberLoraFontFamily(): FontFamily {
    // Lora only ships 400-700 in normal style, so out-of-range weights are registered
    // against the same variable file and will resolve to the nearest available instance.
    val thin = Font(Res.font.lora_variable, weight = FontWeight.Thin)
    val extraLight = Font(Res.font.lora_variable, weight = FontWeight.ExtraLight)
    val light = Font(Res.font.lora_variable, weight = FontWeight.Light)
    val regular = Font(Res.font.lora_variable, weight = FontWeight.Normal)
    val medium = Font(Res.font.lora_variable, weight = FontWeight.Medium)
    val semiBold = Font(Res.font.lora_variable, weight = FontWeight.SemiBold)
    val bold = Font(Res.font.lora_variable, weight = FontWeight.Bold)
    val extraBold = Font(Res.font.lora_variable, weight = FontWeight.ExtraBold)
    val black = Font(Res.font.lora_variable, weight = FontWeight.Black)

    return remember(thin, extraLight, light, regular, medium, semiBold, bold, extraBold, black) {
        FontFamily(thin, extraLight, light, regular, medium, semiBold, bold, extraBold, black)
    }
}

@Composable
private fun rememberPublicSansFontFamily(): FontFamily {
    // The variable file exposes the non-italic Public Sans weight range available on Google Fonts (100-900).
    val thin = Font(Res.font.public_sans_variable, weight = FontWeight.Thin)
    val extraLight = Font(Res.font.public_sans_variable, weight = FontWeight.ExtraLight)
    val light = Font(Res.font.public_sans_variable, weight = FontWeight.Light)
    val regular = Font(Res.font.public_sans_variable, weight = FontWeight.Normal)
    val medium = Font(Res.font.public_sans_variable, weight = FontWeight.Medium)
    val semiBold = Font(Res.font.public_sans_variable, weight = FontWeight.SemiBold)
    val bold = Font(Res.font.public_sans_variable, weight = FontWeight.Bold)
    val extraBold = Font(Res.font.public_sans_variable, weight = FontWeight.ExtraBold)
    val black = Font(Res.font.public_sans_variable, weight = FontWeight.Black)

    return remember(thin, extraLight, light, regular, medium, semiBold, bold, extraBold, black) {
        FontFamily(thin, extraLight, light, regular, medium, semiBold, bold, extraBold, black)
    }
}
