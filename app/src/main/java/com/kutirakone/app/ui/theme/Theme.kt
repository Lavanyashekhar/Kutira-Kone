package com.kutirakone.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Kutira-Kone Brand Colors ──────────────────────────────────────
// Primary: Forest Green (sustainability / nature)
// Secondary: Warm Amber (fabric / warmth)
// Tertiary: Teal (community / exchange)

val Green900  = Color(0xFF1B3A0A)
val Green800  = Color(0xFF27500A)
val Green700  = Color(0xFF3B6D11)
val Green600  = Color(0xFF639922)   // primary brand
val Green100  = Color(0xFFC0DD97)
val Green50   = Color(0xFFEAF3DE)

val Amber600  = Color(0xFFBA7517)
val Amber400  = Color(0xFFEF9F27)
val Amber50   = Color(0xFFFAEEDA)

val Teal600   = Color(0xFF0F6E56)
val Teal400   = Color(0xFF1D9E75)
val Teal50    = Color(0xFFE1F5EE)

val Coral600  = Color(0xFF993C1D)
val Coral400  = Color(0xFFD85A30)
val Coral50   = Color(0xFFFAECE7)

val Purple600 = Color(0xFF534AB7)
val Purple50  = Color(0xFFEEEDFE)

val LightColorScheme = lightColorScheme(
    primary          = Green600,
    onPrimary        = Color.White,
    primaryContainer = Green50,
    onPrimaryContainer = Green800,

    secondary        = Amber600,
    onSecondary      = Color.White,
    secondaryContainer = Amber50,
    onSecondaryContainer = Amber600,

    tertiary         = Teal600,
    onTertiary       = Color.White,
    tertiaryContainer = Teal50,
    onTertiaryContainer = Teal600,

    background       = Color(0xFFFAFAF8),
    surface          = Color.White,
    onBackground     = Color(0xFF1A1A1A),
    onSurface        = Color(0xFF1A1A1A),
    surfaceVariant   = Color(0xFFF1EFE8),
    outline          = Color(0xFFD3D1C7)
)

val DarkColorScheme = darkColorScheme(
    primary          = Green100,
    onPrimary        = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green50,

    secondary        = Amber400,
    onSecondary      = Color(0xFF412402),
    secondaryContainer = Color(0xFF633806),
    onSecondaryContainer = Amber50,

    tertiary         = Teal400,
    onTertiary       = Color(0xFF04342C),
    tertiaryContainer = Teal600,
    onTertiaryContainer = Teal50,

    background       = Color(0xFF111210),
    surface          = Color(0xFF1C1D1A),
    onBackground     = Color(0xFFECECE8),
    onSurface        = Color(0xFFECECE8),
    surfaceVariant   = Color(0xFF2C2C2A),
    outline          = Color(0xFF444441)
)

@Composable
fun KutiraKoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KutiraTypography,
        content     = content
    )
}

// ── Typography ────────────────────────────────────────────────────
// Using system fonts for now; swap out for custom font with
// FontFamily(Font(R.font.your_font)) if needed
val KutiraTypography = androidx.compose.material3.Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.W500, fontSize = 36.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.W500, fontSize = 22.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.W500, fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),   // NFR-05: min 14sp
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.W500, fontSize = 14.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp)
)