package com.example.matharium.fourier

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.matharium.R
import com.example.matharium.app.*

@Composable
fun FourierActionControls(
    running: Boolean,
    onRunningChange: (Boolean) -> Unit,
    hasStarted: Boolean,
    onHasStartedChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    colors: AppColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(AppDesign.spacingSmall)
    ) {
        Button(
            onClick = {
                if (!hasStarted) onHasStartedChange(true)
                onRunningChange(!running)
            },
            modifier = Modifier
                .weight(1f)
                .height(AppDesign.buttonHeight),
            shape = RoundedCornerShape(AppDesign.radiusMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) colors.accentHell else colors.accentCyan
            )
        ) {
            Icon(
                painter = if (running) painterResource(id = R.drawable.pause_outline) else painterResource(id = R.drawable.caret_forward_outline),
                contentDescription = null,
                tint = colors.textOnAccent,
                modifier = Modifier.size(AppDesign.iconSmall)
            )
            Spacer(Modifier.width(AppDesign.spacingSmall))
            Text(
                if (running) "Pause" else if (hasStarted) "Resume" else "Simulate",
                fontWeight = FontWeight.Bold,
                color = colors.textOnAccent,
            )
        }

        AnimatedVisibility(
            visible = hasStarted,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Button(
                onClick = onReset,
                modifier = Modifier.height(AppDesign.buttonHeight),
                shape = RoundedCornerShape(AppDesign.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                border = BorderStroke(AppDesign.borderStandard, colors.accentCyan)
            ) {
                Text("Reset", color = colors.accentCyan)
            }
        }
    }
}
