package com.example.matharium.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isAnimatedBg: Boolean,
    onToggleAnimatedBg: (Boolean) -> Unit,
    onToggleTheme: () -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)
    ) {
        GlassCard(colors = colors) {
            Column(
                modifier = Modifier.padding(AppDesign.spacingLarge)
            ) {
                Text(
                    text = "Appearance",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentCyan
                )
                
                Spacer(modifier = Modifier.height(AppDesign.spacingSmall))

                ToggleRow(
                    label = "Dark Mode",
                    checked = colors.isDark,
                    onCheckedChange = { onToggleTheme() },
                    colors = colors
                )

                Spacer(modifier = Modifier.height(AppDesign.spacingSmall))
                
                ToggleRow(
                    label = "Animated Background",
                    checked = isAnimatedBg,
                    onCheckedChange = onToggleAnimatedBg,
                    colors = colors
                )
                
                Text(
                    text = "Controls whether the background blobs move. Disabling this can save battery.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = AppDesign.spacingExtraSmall)
                )
            }
        }
        
        GlassCard(colors = colors) {
            Column(
                modifier = Modifier.padding(AppDesign.spacingLarge)
            ) {
                Text(
                    text = "About",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentCyan
                )
                
                Spacer(modifier = Modifier.height(AppDesign.spacingSmall))
                
                Text(
                    text = "Matharium v1.0",
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "A playground for mathematical simulations and visualizations.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
