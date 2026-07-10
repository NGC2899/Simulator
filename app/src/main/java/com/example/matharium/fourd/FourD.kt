package com.example.matharium.fourd

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.app.*

@Composable
fun FourDScreen() {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()

    var selectedShape by remember { mutableStateOf(FourDShape.TESSERACT) }
    var isRotating by remember { mutableStateOf(true) }
    var dimensions by remember { mutableIntStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge),
    ) {
        // --- Box 1: Samples and Controls ---
        GlassCard(colors = colors) {
            Column(
                modifier = Modifier.padding(AppDesign.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)
            ) {
                Text(
                    "Geometry Samples",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
                ) {
                    FourDShape.entries.forEach { shape ->
                        val selected = selectedShape == shape
                        FilterChip(
                            selected = selected,
                            onClick = { selectedShape = shape },
                            label = { Text(shape.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accentCyan.copy(alpha = 0.2f),
                                selectedLabelColor = colors.accentCyan
                            )
                        )
                    }
                }

                // Rotation Toggle Button
                Button(
                    onClick = { isRotating = !isRotating },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDesign.buttonHeightSmall),
                    shape = RoundedCornerShape(AppDesign.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRotating) colors.accentCyan else colors.cardSurface,
                        contentColor = if (isRotating) colors.textOnAccent else colors.accentCyan
                    ),
                    border = if (!isRotating) androidx.compose.foundation.BorderStroke(1.dp, colors.accentCyan) else null
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = null,
                        modifier = Modifier.size(AppDesign.iconSmall)
                    )
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text(if (isRotating) "Rotation: ON" else "Rotation: OFF")
                }
            }
        }

        // --- Box 2: Simulation and Dimension Handler ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDesign.canvasHeightSmall)
                .clip(RoundedCornerShape(AppDesign.radiusCard))
                .background(colors.cardSurface.copy(alpha = 0.45f))
                .border(
                    1.dp,
                    colors.cardBorder.copy(alpha = 0.6f),
                    RoundedCornerShape(AppDesign.radiusCard)
                )
        ) {
            // Simulation Area
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Simulating ${selectedShape.name} in ${dimensions}D",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
                // Future implementation: FourDVisualizer(shape = selectedShape, dims = dimensions, rotating = isRotating)
            }

            // Dimension Handler (Right Sidebar)
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
                        IconButton(onClick = { if (dimensions < 10) dimensions++ }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Dimension",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(AppDesign.iconSmallMedium)
                            )
                        }

                        Slider(
                            value = dimensions.toFloat(),
                            onValueChange = { dimensions = it.toInt() },
                            valueRange = 2f..8f,
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

                        IconButton(onClick = { if (dimensions > 2) dimensions-- }) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove Dimension",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(AppDesign.iconSmallMedium)
                            )
                        }

                        Text(
                            text = "${dimensions}D",
                            color = colors.textPrimary,
                            fontSize = AppDesign.textCaption,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
