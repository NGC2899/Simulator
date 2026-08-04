package com.example.matharium.fourier

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.R
import com.example.matharium.app.*
import java.util.Locale

@Composable
fun FourierSettingsCard(
    state: FourierState,
    svgPickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    var isSettingsExpanded by remember { mutableStateOf(false) }
    val colors = state.colors

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

                    AnimatedVisibility(visible = state.displayMode == FourierDisplayMode.WRAPPING) {
                        LabeledSlider(
                            label = "Winding Frequency",
                            valueDisplay = String.format(Locale.US, "%.2f Hz", state.windingFrequency),
                            value = state.windingFrequency,
                            range = 0.1f..5f,
                            colors = colors
                        ) { state.windingFrequency = it }
                    }
                }
            }
        }
    }
}
