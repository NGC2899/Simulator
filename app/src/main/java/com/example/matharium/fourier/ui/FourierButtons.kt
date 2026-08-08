package com.example.matharium.fourier.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.matharium.app.*

@Composable
fun DisplayModeButton(
    imageVector: ImageVector,
    selected: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(AppDesign.sidebarButtonSize)
            .hapticClickable { onClick() },
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        color = if (selected) colors.accentCyan.copy(AppDesign.opacityLow) else colors.cardSurface.copy(
            AppDesign.opacityMedium
        ),
        border = BorderStroke(
            AppDesign.borderThin,
            if (selected) colors.accentCyan else colors.cardBorder.copy(AppDesign.opacityMedium)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(AppDesign.iconMedium),
                tint = if (selected) colors.accentCyan else colors.textSecondary
            )
        }
    }
}

@Composable
fun SidebarActionButton(
    imageVector: ImageVector,
    colors: AppColors,
    modifier: Modifier = Modifier,
    tint: Color = colors.accentHell,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(AppDesign.sidebarButtonSize)
            .clickable { onClick() },
        shape = RoundedCornerShape(AppDesign.radiusSmall),
        color = tint.copy(AppDesign.opacityLow),
        border = BorderStroke(
            AppDesign.borderThin,
            tint.copy(AppDesign.opacityMedium)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(AppDesign.iconMedium),
                tint = tint
            )
        }
    }
}
