package com.example.matharium.fourier.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matharium.R
import com.example.matharium.app.*
import com.example.matharium.fourier.state.SignalInstance

@Composable
fun SignalSettingsCard(
    signal: SignalInstance,
    colors: AppColors,
    showDel: Boolean,
    onParameterChange: () -> Unit,
    onDel: () -> Unit
) {
    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppDesign.spacingMedium).animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth().clickable { signal.isExpanded = !signal.isExpanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val color = signal.colorArgb
                    Box(modifier = Modifier.size(AppDesign.coloredIndicator).background(color, CircleShape).border(1.dp, color.copy(alpha = 0.4f), CircleShape))
                    Spacer(Modifier.width(AppDesign.spacingSmall))
                    Text("Component #${signal.id}", color = colors.textPrimary, fontSize = AppDesign.textHeadline, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { signal.isPaused = !signal.isPaused; onParameterChange() }, modifier = Modifier.size(AppDesign.iconLarge + 4.dp)) { Icon(imageVector = if (signal.isPaused) FluentIcons.MaterialIconsPause else FluentIcons.VscodeCodiconsTriangleRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(AppDesign.iconSmallMedium)) }
                    if (showDel) {
                        Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                        IconButton(onClick = onDel, modifier = Modifier.size(AppDesign.iconLarge + 4.dp)) { Icon(imageVector = FluentIcons.FeatherTrash, null, tint = colors.accentHell.copy(alpha = 0.8f), modifier = Modifier.size(AppDesign.iconSmallMedium)) }
                    }
                    Spacer(Modifier.width(AppDesign.spacingExtraSmall))
                    Icon(imageVector = if (signal.isExpanded) FluentIcons.FeatherChevronUp else FluentIcons.FeatherChevronDown, null, tint = colors.textSecondary, modifier = Modifier.size(AppDesign.iconSmall))
                }
            }
            AnimatedVisibility(visible = signal.isExpanded) {
                Column(modifier = Modifier.padding(top = AppDesign.spacingMedium)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingMedium)) {
                        SignalField(label = "Freq", unit = "Hz", icon = painterResource(id = R.drawable.caret_forward_outline), value = signal.freq, colors = colors, onValueChange = { signal.freq = it; signal.initialFreq = it; signal.updateCache(); onParameterChange() })
                        SignalField(label = "Amp", unit = "", icon = painterResource(id = R.drawable.add_outline), value = signal.amp, colors = colors, onValueChange = { signal.amp = it; signal.initialAmp = it; signal.updateCache(); onParameterChange() })
                        SignalField(label = "Phase", unit = "°", icon = rememberVectorPainter(Icons.Default.Refresh), value = signal.phase, colors = colors, onValueChange = { signal.phase = it; signal.initialPhase = it; signal.updateCache(); onParameterChange() })
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SignalField(label: String, unit: String, icon: Painter, value: String, colors: AppColors, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text("$label ($unit)", color = colors.textSecondary, fontSize = AppDesign.textOverline, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(AppDesign.spacingSmall))
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold), cursorBrush = SolidColor(colors.accentCyan), modifier = Modifier.fillMaxWidth().background(colors.cardSurface.copy(alpha = 0.15f), RoundedCornerShape(AppDesign.radiusSmall)).border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall)).padding(horizontal = AppDesign.spacingSmall, vertical = AppDesign.spacingSmall))
    }
}
