package com.example.matharium.app

import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  DESIGN TOKENS
// ─────────────────────────────────────────────

object AppDesign {
    val radiusSmall = 8.dp
    val radiusButton = 10.dp
    val radiusMedium = 12.dp
    val radiusLarge = 16.dp
    val radiusCard = 20.dp

    val spacingTiny = 2.dp
    val spacingExtraSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 12.dp
    val spacingLarge = 16.dp

    val buttonHeight = 50.dp
    val buttonHeightSmall = 38.dp
    val chipHeight = 32.dp
    val drawingAreaHeight = 200.dp
    val textFieldHeight = 56.dp
    val canvasHeightSmall = 300.dp
    val sidebarButtonSize = 42.dp
    val navBarHeight = 60.dp
    val welcomeHeaderHeight = 100.dp
    val welcomeSectionSpacing = 25.dp
    val welcomeGridSpacing = 60.dp
    val termsBoxHeight = 280.dp
    val errorGradientThreshold = 20f
    val unitCircleRadius = 110.dp
    val phasorRadiusBase = 20.dp

    val iconTiny = 10.dp
    val iconSmall = 16.dp
    val iconSmallMedium = 18.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconWelcome = 80.dp

    val textCaption = 9.sp
    val textOverline = 10.sp
    val textSmall = 11.sp
    val textBody = 12.sp
    val textBodyLarge = 13.sp
    val textHeadline = 15.sp
    val textHeadlineLarge = 28.sp
    val textDisplayLarge = 60.sp

    val borderNone = 0.dp
    val borderThin = 1.dp
    val borderStandard = 2.dp
    val strokeThin = 1.dp
    val strokeStandard = 2.dp
    val strokeThick = 2.5.dp

    val opacitySubtle = 0.05f
    val opacityLow = 0.15f
    val opacityMedium = 0.4f
    val opacityHigh = 0.7f
    val opacityGlass = 0.45f
    val opacityGlassBorder = 0.6f
    val opacityTrace = 0.8f
    val opacityGrid = 0.05f

    val animDurationFast = 150
    val animDurationStandard = 300
    val animDurationSlow = 500

    val coloredIndicator = 12.dp
}

// ─────────────────────────────────────────────
//  COLOR TOKENS  ← edit these to retheme the app
// ─────────────────────────────────────────────

object DarkColors : AppColors {
    // Backgrounds
    override val bgTop:          Color = Color(0xFF060B18)   // deep void navy
    override val bgBottom:       Color = Color(0xFF0D1126)   // slightly lighter navy

    // Glass card surface (use with alpha)
    override val cardSurface:    Color = Color(0xFF1A2040)   // translucent panel base
    override val cardBorder:     Color = Color(0xFF2A3560)   // subtle border glow

    // Accents
    override val accentCyan:     Color = Color(0xFF00F5D4)   // electric cyan
    override val accentViolet:   Color = Color(0xFF9619DA)   // deep violet
    override val accentHell:     Color = Color(0xFFF5007F)   // hell-mode red

    // Text
    override val textPrimary:    Color = Color(0xFFE2EAF4)   // near-white
    override val textSecondary:  Color = Color(0xFF7A8BB0)   // muted slate
    override val textOnAccent:   Color = Color(0xFF060B18)   // dark text on bright buttons

    // Input fields
    override val fieldBorder:    Color = Color(0xFF2A3560)
    override val fieldFocused:   Color = Color(0xFF00F5D4)
    override val fieldLabel:     Color = Color(0xFF7A8BB0)

    // Pendulum canvas
    override val pendulum:   Color = Color(0xFFEEEEEE)
    override val trailStart:     Color = Color(0x8000F5D4)   // trail fade-in
    override val trailEnd:       Color = Color(0x00000000)   // trail fade-out
    override val pivot:          Color = Color(0xFFFFFFFF)

    override val isDark:         Boolean = true
}

object LightColors : AppColors {
    // Backgrounds
    override val bgTop:          Color = Color(0xFFE8EEF8)   // icy blue-white
    override val bgBottom:       Color = Color(0xFFF4F7FF)   // cool off-white

    // Glass card surface
    override val cardSurface:    Color = Color(0xFFFFFFFF)
    override val cardBorder:     Color = Color(0xFFCDD5E8)

    // Accents
    override val accentCyan:     Color = Color(0xFF0891B2)   // teal
    override val accentViolet:   Color = Color(0xFF9F29E0)   // same violet
    override val accentHell:     Color = Color(0xFFFF62B4)

    // Text
    override val textPrimary:    Color = Color(0xFF1E293B)
    override val textSecondary:  Color = Color(0xFF64748B)
    override val textOnAccent:   Color = Color(0xFFFFFFFF)

    // Input fields
    override val fieldBorder:    Color = Color(0xFFCDD5E8)
    override val fieldFocused:   Color = Color(0xFF0891B2)
    override val fieldLabel:     Color = Color(0xFF64748B)

    // Pendulum canvas
    override val pendulum:   Color = Color(0xFF151515)
    override val trailStart:     Color = Color(0x800891B2)
    override val trailEnd:       Color = Color(0x00000000)
    override val pivot:          Color = Color(0xFF1E293B)

    override val isDark:         Boolean = false
}

// ─────────────────────────────────────────────
//  Interface & LocalComposition
// ─────────────────────────────────────────────

interface AppColors {
    val bgTop:          Color
    val bgBottom:       Color
    val cardSurface:    Color
    val cardBorder:     Color
    val accentCyan:     Color
    val accentViolet:   Color
    val accentHell:     Color
    val textPrimary:    Color
    val textSecondary:  Color
    val textOnAccent:   Color
    val fieldBorder:    Color
    val fieldFocused:   Color
    val fieldLabel:     Color
    val pendulum:   Color
    val trailStart:     Color
    val trailEnd:       Color
    val pivot:          Color
    val isDark:         Boolean
}

val LocalAppColors = staticCompositionLocalOf<AppColors> { DarkColors }

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) {
        androidx.compose.material3.darkColorScheme(
            primary = colors.accentCyan,
            secondary = colors.accentViolet,
            tertiary = colors.accentHell,
            surface = colors.bgTop,
            background = colors.bgTop,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
            surfaceVariant = colors.cardSurface,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.cardBorder,
            error = colors.accentHell
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = colors.accentCyan,
            secondary = colors.accentViolet,
            tertiary = colors.accentHell,
            surface = colors.bgTop,
            background = colors.bgTop,
            onSurface = colors.textPrimary,
            onBackground = colors.textPrimary,
            surfaceVariant = colors.cardSurface,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.cardBorder,
            error = colors.accentHell
        )
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = colorScheme
        ) {
            val style = TextStyle(
                color = colors.textPrimary,
                fontSize = AppDesign.textBodyLarge,
                fontWeight = FontWeight.Normal
            )
            ProvideTextStyle(value = style) {
                content()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  GLASS CARD COMPONENT
// ─────────────────────────────────────────────

@Composable
fun GlassCard(
    colors: AppColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDesign.radiusCard))
            .background(colors.cardSurface.copy(alpha = AppDesign.opacityGlass))
            .border(
                AppDesign.borderThin,
                colors.cardBorder.copy(alpha = AppDesign.opacityGlassBorder),
                RoundedCornerShape(AppDesign.radiusCard)
            )
    ) {
        content()
    }
}

// ─────────────────────────────────────────────
//  SHARED ANIMATED BLOB BACKGROUND
// ─────────────────────────────────────────────

data class BlobAnimParams(
    val xStart: Float, val xEnd: Float,
    val yStart: Float, val yEnd: Float,
    val duration: Int
)

@Composable
fun AnimatedBlobBackground(
    blob2Color: Color,
    blob1Color: Color,
    blob1Alpha: Float = AppDesign.opacityLow,
    blob2Alpha: Float = AppDesign.opacityLow,
    blob1Size: Float = 800f,
    blob2Size: Float = 600f,
    label: String = "blobs",
    content: @Composable BoxScope.() -> Unit
) {
    val animParams = androidx.compose.runtime.remember {
        Pair(
            BlobAnimParams(
                xStart = kotlin.random.Random.nextFloat() * 400f - 200f,
                xEnd = kotlin.random.Random.nextFloat() * 400f - 200f,
                yStart = kotlin.random.Random.nextFloat() * 600f,
                yEnd = kotlin.random.Random.nextFloat() * 600f,
                duration = kotlin.random.Random.nextInt(8000, 15000)
            ),
            BlobAnimParams(
                xStart = kotlin.random.Random.nextFloat() * 400f - 200f,
                xEnd = kotlin.random.Random.nextFloat() * 400f - 200f,
                yStart = kotlin.random.Random.nextFloat() * 600f,
                yEnd = kotlin.random.Random.nextFloat() * 600f,
                duration = kotlin.random.Random.nextInt(10000, 18000)
            )
        )
    }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = label)
    val b1X by infiniteTransition.animateFloat(
        initialValue = animParams.first.xStart, targetValue = animParams.first.xEnd,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(animParams.first.duration),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "${label}_b1x"
    )
    val b1Y by infiniteTransition.animateFloat(
        initialValue = animParams.first.yStart, targetValue = animParams.first.yEnd,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(animParams.first.duration + 2000),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "${label}_b1y"
    )
    val b2X by infiniteTransition.animateFloat(
        initialValue = animParams.second.xStart, targetValue = animParams.second.xEnd,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(animParams.second.duration),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "${label}_b2x"
    )
    val b2Y by infiniteTransition.animateFloat(
        initialValue = animParams.second.yStart, targetValue = animParams.second.yEnd,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(animParams.second.duration + 3000),
            androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "${label}_b2y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = b1X.dp, y = b1Y.dp)
                .size(blob1Size.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(blob1Color.copy(alpha = blob1Alpha), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .offset(x = b2X.dp, y = b2Y.dp)
                .size(blob2Size.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(blob2Color.copy(alpha = blob2Alpha), Color.Transparent)
                    )
                )
        )
        content()
    }
}

// ─────────────────────────────────────────────
//  SHARED UI COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun LabeledSlider(
    label: String,
    valueDisplay: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    colors: AppColors,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = AppDesign.spacingSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = label,
                color = colors.textPrimary,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.material3.Text(
                text = valueDisplay,
                color = colors.accentCyan,
                fontSize = AppDesign.textBody,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(colors.accentCyan.copy(AppDesign.opacityLow), RoundedCornerShape(AppDesign.radiusSmall))
                    .padding(horizontal = AppDesign.spacingSmall, vertical = AppDesign.spacingTiny)
            )
        }
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = colors.accentCyan,
                activeTrackColor = colors.accentCyan,
                inactiveTrackColor = colors.fieldBorder.copy(AppDesign.opacityMedium)
            ),
            modifier = Modifier.padding(top = AppDesign.spacingTiny)
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDesign.textFieldHeight)
            .clip(RoundedCornerShape(AppDesign.radiusSmall))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = AppDesign.spacingExtraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = null, // Let the Row handle the click
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = colors.accentCyan,
                uncheckedColor = colors.fieldBorder,
                checkmarkColor = colors.textOnAccent
            )
        )
        Spacer(Modifier.width(AppDesign.spacingSmall))
        androidx.compose.material3.Text(
            text = label,
            color = colors.textPrimary,
            fontSize = AppDesign.textBodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DisplayModeButton(
    icon: ImageVector,
    selected: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(AppDesign.sidebarButtonSize)
            .clickable { onClick() },
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDesign.iconMedium),
                tint = if (selected) colors.accentCyan else colors.textSecondary
            )
        }
    }
}

@Composable
fun SidebarActionButton(
    icon: Painter,
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
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDesign.iconMedium),
                tint = tint
            )
        }
    }
}
