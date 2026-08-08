package com.example.matharium.fourier.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.matharium.app.*
import com.example.matharium.fourier.engine.FourierExportLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourierExportDialog(
    terms: List<FourierExportLogic.TermData>,
    is2D: Boolean,
    colors: AppColors,
    onDismiss: () -> Unit
) {
    val normalSeries = remember(terms, is2D) { FourierExportLogic.generateNormalSeries(terms, is2D) }
    val complexSeries = remember(terms) { FourierExportLogic.generateComplexSeries(terms) }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = 0.98f))
            .border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.3f), RoundedCornerShape(AppDesign.radiusCard)),
        title = {
            Text(
                "Export Fourier Series",
                color = colors.textPrimary,
                fontSize = AppDesign.textHeadline,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDesign.spacingLarge)) {
                ExportField(
                    label = "Normal Series",
                    content = normalSeries,
                    colors = colors,
                    onCopy = { clipboardManager.setText(AnnotatedString(normalSeries)) }
                )
                ExportField(
                    label = "Complex Series",
                    content = complexSeries,
                    colors = colors,
                    onCopy = { clipboardManager.setText(AnnotatedString(complexSeries)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.accentCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    )
}

@Composable
fun ExportField(label: String, content: String, colors: AppColors, onCopy: () -> Unit) {
    val scrollState = rememberScrollState()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = colors.textSecondary,
                fontSize = AppDesign.textOverline,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onCopy, modifier = Modifier.height(32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ContentCopy,
                        null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Copy", color = colors.accentCyan, fontSize = AppDesign.textOverline, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(AppDesign.spacingExtraSmall))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
                .background(colors.cardSurface.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall))
                .border(AppDesign.borderThin, colors.cardBorder.copy(alpha = 0.2f), RoundedCornerShape(AppDesign.radiusSmall))
                .verticalScroll(scrollState)
                .padding(AppDesign.spacingSmall)
        ) {
            SelectionContainer {
                Text(
                    content,
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.textPrimary,
                    fontSize = AppDesign.textSmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
