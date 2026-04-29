package com.eahm.drinkwaterapp.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

internal val PlusJakartaSans = FontFamily(
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Normal),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Medium),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Bold),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.ExtraBold),
)

internal val Nunito = FontFamily(
    Font(GoogleFont("Nunito"), provider, FontWeight.Normal),
    Font(GoogleFont("Nunito"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Nunito"), provider, FontWeight.Bold),
    Font(GoogleFont("Nunito"), provider, FontWeight.ExtraBold),
)

internal val JetBrainsMono: FontFamily = FontFamily(
    Font(GoogleFont("JetBrains Mono"), provider, FontWeight.Normal),
    Font(GoogleFont("JetBrains Mono"), provider, FontWeight.Medium),
)

val DwTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 66.sp,
        letterSpacing = (-0.02).em,
    ),
    displayMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.02).em,
    ),
    displaySmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 51.sp,
        letterSpacing = (-0.02).em,
    ),

    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.02).em,
    ),

    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 29.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.06.em,
    ),
    labelMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.06.em,
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.10.em,
    ),
)

data class DwExtendedTypography(
    val numeric: TextStyle = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum",
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

internal val LocalDwExtendedTypography = staticCompositionLocalOf { DwExtendedTypography() }

val MaterialTheme.typographyExtended: DwExtendedTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalDwExtendedTypography.current
