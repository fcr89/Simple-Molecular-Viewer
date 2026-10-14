package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BioDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFF9CF4FF),
    secondary = MagentaSecondary,
    onSecondary = Color(0xFF5A0028),
    secondaryContainer = Color(0xFF80003D),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = AmberTertiary,
    onTertiary = Color(0xFF452B00),
    background = BioDarkBg,
    onBackground = Color(0xFFE2E8F0),
    surface = BioSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = BioSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Use our scientific dark palette for consistent molecular contrast
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
