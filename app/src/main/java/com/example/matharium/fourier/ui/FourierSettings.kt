package com.example.matharium.fourier.ui

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.app.*
import com.example.matharium.fourier.state.*

@Composable
fun FourierSettingsCard(
    state: FourierState,
    svgPickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    var isSettingsExpanded by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .padding(AppDesign.spacingLarge)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSettingsExpanded = !isSettingsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Signal Settings",
                    fontSize = AppDesign.textHeadline,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isSettingsExpanded) FluentIcons.FeatherChevronUp else FluentIcons.FeatherChevronDown,
                    null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(AppDesign.iconSmall)
                )
            }

            WaveTypeSelector(state, svgPickerLauncher)

            AnimatedVisibility(visible = isSettingsExpanded) {
                Column {
                    when (state.waveType) {
                        WaveType.MY_SIGNAL, WaveType.MY_SIGNAL_2D -> DrawingCanvas(state)
                        WaveType.PURE_SIGNAL, WaveType.FORMULA -> CustomSignalSettings(state)
                        WaveType.SVG -> SVGSettings(state, svgPickerLauncher)
                        else -> {}
                    }
                }
            }
        }
    }
}
