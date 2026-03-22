package com.example.thulur_front.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ThulurShadeScale(
    val s50: Color,
    val s100: Color,
    val s300: Color,
    val s500: Color,
    val s700: Color,
    val s900: Color,
    val s950: Color,
    val s300A08: Color = Color.Unspecified,
    val s300A10: Color = Color.Unspecified,
)

@Immutable
data class ThulurColors(
    val primary: ThulurShadeScale,
    val error: ThulurShadeScale,
    val success: ThulurShadeScale,
    val warning: ThulurShadeScale,
    val slate: ThulurShadeScale,
)

internal val DefaultThulurColors = ThulurColors(
    primary = ThulurShadeScale(
        s50 = Color(0xFFEFF6FF),
        s100 = Color(0xFFDBEAFE),
        s300 = Color(0xFF93C5FD),
        s500 = Color(0xFF3B82F6),
        s700 = Color(0xFF1D4ED8),
        s900 = Color(0xFF1E3A8A),
        s950 = Color(0xFF172554),
        s300A08 = Color(0x1493C5FD),
        s300A10 = Color(0x1A93C5FD),
    ),
    error = ThulurShadeScale(
        s50 = Color(0xFFFEF2F2),
        s100 = Color(0xFFFEE2E2),
        s300 = Color(0xFFFCA5A5),
        s500 = Color(0xFFEF4444),
        s700 = Color(0xFFB91C1C),
        s900 = Color(0xFF7F1D1D),
        s950 = Color(0xFF450A0A),
    ),
    success = ThulurShadeScale(
        s50 = Color(0xFFF0FDF4),
        s100 = Color(0xFFDCFCE7),
        s300 = Color(0xFF86EFAC),
        s500 = Color(0xFF22C55E),
        s700 = Color(0xFF15803D),
        s900 = Color(0xFF14532D),
        s950 = Color(0xFF052E16),
    ),
    warning = ThulurShadeScale(
        s50 = Color(0xFFFFFBEB),
        s100 = Color(0xFFFEF3C7),
        s300 = Color(0xFFFCD34D),
        s500 = Color(0xFFF59E0B),
        s700 = Color(0xFFB45309),
        s900 = Color(0xFF78350F),
        s950 = Color(0xFF451A03),
    ),
    slate = ThulurShadeScale(
        s50 = Color(0xFFF8FAFC),
        s100 = Color(0xFFF1F5F9),
        s300 = Color(0xFFCBD5E1),
        s500 = Color(0xFF64748B),
        s700 = Color(0xFF334155),
        s900 = Color(0xFF0F172A),
        s950 = Color(0xFF020617),
        s300A08 = Color(0x14CBD5E1),
        s300A10 = Color(0x1ACBD5E1),
    ),
)
