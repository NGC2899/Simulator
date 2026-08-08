package com.example.matharium.fourier.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.matharium.R
import com.example.matharium.app.*
import com.example.matharium.fourier.state.FourierDisplayMode

@Composable
fun BoxScope.FourierLeftSidebar(
    displayMode: FourierDisplayMode,
    onDisplayModeChange: (FourierDisplayMode) -> Unit,
    onClearPath: () -> Unit,
    colors: AppColors
) {
    Column(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = AppDesign.spacingMedium),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall + AppDesign.spacingExtraSmall / 2f)
    ) {
        DisplayModeButton(
            imageVector = FluentIcons.TablerCirclesRelation,
            selected = displayMode == FourierDisplayMode.CIRCULAR,
            colors = colors
        ) {
            if (displayMode != FourierDisplayMode.CIRCULAR) onClearPath()
            onDisplayModeChange(FourierDisplayMode.CIRCULAR)
        }

        DisplayModeButton(
            imageVector = Icons.Default.Adjust,
            selected = displayMode == FourierDisplayMode.WRAPPING,
            colors = colors
        ) {
            if (displayMode != FourierDisplayMode.WRAPPING) onClearPath()
            onDisplayModeChange(FourierDisplayMode.WRAPPING)
        }

        DisplayModeButton(
            imageVector = FluentIcons.MaterialSymbolsCircles,
            selected = displayMode == FourierDisplayMode.COMPLEX,
            colors = colors
        ) {
            if (displayMode != FourierDisplayMode.COMPLEX) onClearPath()
            onDisplayModeChange(FourierDisplayMode.COMPLEX)
        }

        Spacer(Modifier.height(AppDesign.spacingSmall + AppDesign.spacingExtraSmall / 2f))

        SidebarActionButton(
            imageVector = FluentIcons.TablerClearAll,
            colors = colors,
            onClick = { onClearPath() }
        )
    }
}

@Composable
fun BoxScope.FourierRightSidebar(
    displayNTermsProvider: () -> Int,
    onDisplayNTermsChange: (Int) -> Unit,
    onActiveNTermsChange: (Int) -> Unit,
    colors: AppColors
) {
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = AppDesign.spacingMedium)
            .width(AppDesign.sidebarButtonSize)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(AppDesign.termsBoxHeight)
                .background(
                    colors.cardSurface.copy(alpha = AppDesign.opacityMedium),
                    RoundedCornerShape(AppDesign.radiusCard)
                )
                .border(
                    AppDesign.borderThin,
                    colors.cardBorder.copy(alpha = AppDesign.opacityLow * 2f),
                    RoundedCornerShape(AppDesign.radiusCard)
                )
                .padding(vertical = AppDesign.spacingMedium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                IconButton(
                    onClick = { 
                        val current = displayNTermsProvider()
                        if (current < 250) {
                            val next = current + 1
                            onDisplayNTermsChange(next)
                            onActiveNTermsChange(next)
                        }
                    },
                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_outline),
                        null,
                        tint = colors.accentCyan,
                    )
                }

                val sliderValue = displayNTermsProvider()
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = { onDisplayNTermsChange(it.toInt()) },
                    onValueChangeFinished = { onActiveNTermsChange(sliderValue) },
                    valueRange = 1f..250f,
                    modifier = Modifier
                        .weight(1f)
                        .layout { measurable: Measurable, constraints ->
                            val placeable = measurable.measure(
                                constraints.copy(
                                    minWidth = constraints.minHeight,
                                    maxWidth = constraints.maxHeight,
                                    minHeight = constraints.minWidth,
                                    maxHeight = constraints.maxWidth
                                )
                            )
                            layout(placeable.height, placeable.width) {
                                placeable.placeWithLayer(
                                    x = -(placeable.width - placeable.height) / 2,
                                    y = (placeable.width - placeable.height) / 2
                                ) {
                                    rotationZ = -90f
                                }
                            }
                        },
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accentCyan,
                        activeTrackColor = colors.accentCyan,
                        inactiveTrackColor = colors.fieldBorder.copy(alpha = AppDesign.opacityLow * 2f)
                    )
                )

                IconButton(
                    onClick = { 
                        val current = displayNTermsProvider()
                        if (current > 1) {
                            val next = current - 1
                            onDisplayNTermsChange(next)
                            onActiveNTermsChange(next)
                        }
                    },
                    modifier = Modifier.size(AppDesign.iconSmallMedium)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.remove),
                        null,
                        tint = colors.accentCyan,
                    )
                }

                Text(
                    text = displayNTermsProvider().toString(),
                    color = colors.textPrimary,
                    fontSize = AppDesign.textCaption,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = AppDesign.spacingTiny)
                )
            }
        }
    }
}
