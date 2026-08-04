package com.example.matharium.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * High-precision implementation of Microsoft Fluent UI System Icons.
 * Calibrated for a 24x24 viewport.
 */
object FluentIcons {

    private const val K = 0.55228475f // Magic number for circle approximation

    val PhosphorWaveSquare: ImageVector
        get() {
            if (_PhosphorWaveSquare != null) return _PhosphorWaveSquare!!

            _PhosphorWaveSquare = ImageVector.Builder(
                name = "wave-square",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 256f,
                viewportHeight = 256f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(240f, 128f)
                    verticalLineToRelative(56f)
                    arcToRelative(8f, 8f, 0f, false, true, -8f, 8f)
                    horizontalLineTo(128f)
                    arcToRelative(8f, 8f, 0f, false, true, -8f, -8f)
                    verticalLineTo(80f)
                    horizontalLineTo(32f)
                    verticalLineToRelative(48f)
                    arcToRelative(8f, 8f, 0f, false, true, -16f, 0f)
                    verticalLineTo(72f)
                    arcToRelative(8f, 8f, 0f, false, true, 8f, -8f)
                    horizontalLineTo(128f)
                    arcToRelative(8f, 8f, 0f, false, true, 8f, 8f)
                    verticalLineTo(176f)
                    horizontalLineToRelative(88f)
                    verticalLineTo(128f)
                    arcToRelative(8f, 8f, 0f, false, true, 16f, 0f)
                    close()
                }
            }.build()

            return _PhosphorWaveSquare!!
        }

    private var _PhosphorWaveSquare: ImageVector? = null

    val BootstrapSoundwave: ImageVector
        get() {
            if (_BootstrapSoundwave != null) return _BootstrapSoundwave!!

            _BootstrapSoundwave = ImageVector.Builder(
                name = "soundwave",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(8.5f, 2f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, 0.5f)
                    verticalLineToRelative(11f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineToRelative(-11f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(-2f, 2f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, 0.5f)
                    verticalLineToRelative(7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineToRelative(-7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(4f, 0f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, 0.5f)
                    verticalLineToRelative(7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineToRelative(-7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(-6f, 1.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 5f, 6f)
                    verticalLineToRelative(4f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineTo(6f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(8f, 0f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, 0.5f)
                    verticalLineToRelative(4f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineTo(6f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(-10f, 1f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 3f, 7f)
                    verticalLineToRelative(2f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineTo(7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                    moveToRelative(12f, 0f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, 0.5f)
                    verticalLineToRelative(2f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                    verticalLineTo(7f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, 0.5f, -0.5f)
                }
            }.build()

            return _BootstrapSoundwave!!
        }

    private var _BootstrapSoundwave: ImageVector? = null

    val FluentuiSystemIconsDataLine: ImageVector
        get() {
            if (_FluentuiSystemIconsDataLine != null) return _FluentuiSystemIconsDataLine!!

            _FluentuiSystemIconsDataLine = ImageVector.Builder(
                name = "data-line",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(16f, 6f)
                    curveTo(16f, 4.34315f, 17.3431f, 3f, 19f, 3f)
                    curveTo(20.6569f, 3f, 22f, 4.34315f, 22f, 6f)
                    curveTo(22f, 7.65685f, 20.6569f, 9f, 19f, 9f)
                    curveTo(18.8382f, 9f, 18.6794f, 8.98719f, 18.5246f, 8.96254f)
                    lineTo(16.4865f, 12.3209f)
                    curveTo(16.8107f, 12.8001f, 17f, 13.3779f, 17f, 14f)
                    curveTo(17f, 15.6569f, 15.6569f, 17f, 14f, 17f)
                    curveTo(13.0971f, 17f, 12.2874f, 16.6012f, 11.7374f, 15.9701f)
                    lineTo(7.99584f, 17.8406f)
                    curveTo(7.9986f, 17.8934f, 8f, 17.9465f, 8f, 18f)
                    curveTo(8f, 19.6569f, 6.65685f, 21f, 5f, 21f)
                    curveTo(3.34315f, 21f, 2f, 19.6569f, 2f, 18f)
                    curveTo(2f, 16.3431f, 3.34315f, 15f, 5f, 15f)
                    curveTo(6.06616f, 15f, 7.00242f, 15.5562f, 7.5345f, 16.3942f)
                    lineTo(11.066f, 14.6287f)
                    curveTo(11.0228f, 14.426f, 11f, 14.2156f, 11f, 14f)
                    curveTo(11f, 12.3431f, 12.3431f, 11f, 14f, 11f)
                    curveTo(14.4823f, 11f, 14.938f, 11.1138f, 15.3417f, 11.316f)
                    lineTo(17.1395f, 8.35358f)
                    curveTo(16.4454f, 7.80411f, 16f, 6.95406f, 16f, 6f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsDataLine!!
        }

    private var _FluentuiSystemIconsDataLine: ImageVector? = null

    val FluentuiSystemIconsCubeMultiple: ImageVector
        get() {
            if (_FluentuiSystemIconsCubeMultiple != null) return _FluentuiSystemIconsCubeMultiple!!

            _FluentuiSystemIconsCubeMultiple = ImageVector.Builder(
                name = "cube-multiple",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(10.5186f, 4.25082f)
                    curveTo(11.4779f, 3.91639f, 12.5221f, 3.91639f, 13.4814f, 4.25082f)
                    curveTo(13.5172f, 4.26257f, 13.5529f, 4.27476f, 13.5884f, 4.28738f)
                    lineTo(19.5032f, 6.38618f)
                    curveTo(19.8787f, 6.51941f, 20.2151f, 6.72229f, 20.5f, 6.97722f)
                    verticalLineTo(6.56209f)
                    curveTo(20.5f, 5.71537f, 19.9668f, 4.96039f, 19.1688f, 4.67724f)
                    lineTo(14.0065f, 2.84543f)
                    curveTo(12.7085f, 2.38486f, 11.2915f, 2.38486f, 9.99354f, 2.84543f)
                    lineTo(4.83118f, 4.67724f)
                    curveTo(4.0332f, 4.96039f, 3.5f, 5.71537f, 3.5f, 6.56209f)
                    verticalLineTo(6.97722f)
                    curveTo(3.78489f, 6.72229f, 4.12129f, 6.51941f, 4.49677f, 6.38618f)
                    lineTo(10.4116f, 4.28738f)
                    curveTo(10.4471f, 4.27476f, 10.4828f, 4.26257f, 10.5186f, 4.25082f)
                    close()
                    moveTo(6.44943f, 10.1112f)
                    curveTo(6.58795f, 9.72081f, 7.01669f, 9.51664f, 7.40706f, 9.65516f)
                    lineTo(12f, 11.2849f)
                    lineTo(16.5929f, 9.65516f)
                    curveTo(16.9833f, 9.51664f, 17.4121f, 9.72081f, 17.5506f, 10.1112f)
                    curveTo(17.6891f, 10.5015f, 17.4849f, 10.9303f, 17.0946f, 11.0688f)
                    lineTo(12.75f, 12.6104f)
                    verticalLineTo(17.3307f)
                    curveTo(12.75f, 17.7449f, 12.4142f, 18.0807f, 12f, 18.0807f)
                    curveTo(11.5858f, 18.0807f, 11.25f, 17.7449f, 11.25f, 17.3307f)
                    verticalLineTo(12.6104f)
                    lineTo(6.90544f, 11.0688f)
                    curveTo(6.51508f, 10.9303f, 6.31091f, 10.5015f, 6.44943f, 10.1112f)
                    close()
                    moveTo(10.746f, 5.22957f)
                    curveTo(11.5572f, 4.94171f, 12.4428f, 4.94171f, 13.254f, 5.22957f)
                    lineTo(19.1688f, 7.32836f)
                    curveTo(19.9668f, 7.61152f, 20.5f, 8.36649f, 20.5f, 9.21322f)
                    verticalLineTo(17.4476f)
                    curveTo(20.5f, 18.2943f, 19.9668f, 19.0493f, 19.1688f, 19.3324f)
                    lineTo(13.254f, 21.4312f)
                    curveTo(12.4428f, 21.7191f, 11.5572f, 21.7191f, 10.746f, 21.4312f)
                    lineTo(4.83118f, 19.3324f)
                    curveTo(4.0332f, 19.0493f, 3.5f, 18.2943f, 3.5f, 17.4476f)
                    verticalLineTo(9.21322f)
                    curveTo(3.5f, 8.36649f, 4.0332f, 7.61152f, 4.83118f, 7.32836f)
                    lineTo(10.746f, 5.22957f)
                    close()
                    moveTo(12.7524f, 6.64321f)
                    curveTo(12.2657f, 6.47049f, 11.7343f, 6.47049f, 11.2476f, 6.64321f)
                    lineTo(5.33279f, 8.742f)
                    curveTo(5.1333f, 8.81279f, 5f, 9.00153f, 5f, 9.21322f)
                    verticalLineTo(17.4476f)
                    curveTo(5f, 17.6593f, 5.1333f, 17.848f, 5.3328f, 17.9188f)
                    lineTo(11.2476f, 20.0176f)
                    curveTo(11.7343f, 20.1903f, 12.2657f, 20.1903f, 12.7524f, 20.0176f)
                    lineTo(18.6672f, 17.9188f)
                    curveTo(18.8667f, 17.848f, 19f, 17.6593f, 19f, 17.4476f)
                    verticalLineTo(9.21322f)
                    curveTo(19f, 9.00154f, 18.8667f, 8.81279f, 18.6672f, 8.742f)
                    lineTo(12.7524f, 6.64321f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsCubeMultiple!!
        }

    private var _FluentuiSystemIconsCubeMultiple: ImageVector? = null

    val FluentuiSystemIconsSettingsCogMultiple: ImageVector
        get() {
            if (_FluentuiSystemIconsSettingsCogMultiple != null) return _FluentuiSystemIconsSettingsCogMultiple!!

            _FluentuiSystemIconsSettingsCogMultiple = ImageVector.Builder(
                name = "settings-cog-multiple",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(17.996f, 13f)
                    curveTo(18.164f, 13f, 18.3304f, 13.0083f, 18.4941f, 13.0244f)
                    curveTo(18.7918f, 13.0537f, 19.0223f, 13.2866f, 19.0849f, 13.5791f)
                    lineTo(19.3505f, 14.8223f)
                    curveTo(19.4197f, 15.146f, 19.7543f, 15.339f, 20.0693f, 15.2373f)
                    lineTo(21.2783f, 14.8457f)
                    curveTo(21.5623f, 14.7538f, 21.8781f, 14.8369f, 22.0527f, 15.0791f)
                    curveTo(22.2458f, 15.3472f, 22.4131f, 15.6352f, 22.5507f, 15.9395f)
                    curveTo(22.6739f, 16.2118f, 22.5879f, 16.5271f, 22.3662f, 16.7275f)
                    lineTo(21.4218f, 17.5801f)
                    curveTo(21.1763f, 17.802f, 21.1763f, 18.1883f, 21.4218f, 18.4102f)
                    lineTo(22.3691f, 19.2646f)
                    curveTo(22.5908f, 19.4648f, 22.6773f, 19.7804f, 22.5546f, 20.0527f)
                    curveTo(22.4178f, 20.3565f, 22.2516f, 20.6443f, 22.0595f, 20.9121f)
                    curveTo(21.8851f, 21.155f, 21.5687f, 21.2385f, 21.2841f, 21.1465f)
                    lineTo(20.0693f, 20.7529f)
                    curveTo(19.7543f, 20.6512f, 19.4197f, 20.8442f, 19.3505f, 21.168f)
                    lineTo(19.083f, 22.4199f)
                    curveTo(19.0203f, 22.7125f, 18.7899f, 22.9458f, 18.4921f, 22.9756f)
                    curveTo(18.3291f, 22.9919f, 18.1633f, 23f, 17.996f, 23f)
                    curveTo(17.8301f, 23f, 17.6657f, 22.9915f, 17.5039f, 22.9756f)
                    curveTo(17.2061f, 22.946f, 16.9757f, 22.7136f, 16.913f, 22.4209f)
                    lineTo(16.6455f, 21.168f)
                    curveTo(16.5762f, 20.8441f, 16.2418f, 20.6509f, 15.9267f, 20.7529f)
                    lineTo(14.708f, 21.1465f)
                    curveTo(14.4235f, 21.2381f, 14.1068f, 21.155f, 13.9326f, 20.9121f)
                    curveTo(13.7411f, 20.6451f, 13.575f, 20.3583f, 13.4384f, 20.0557f)
                    curveTo(13.3157f, 19.7834f, 13.4024f, 19.4678f, 13.624f, 19.2676f)
                    lineTo(14.5742f, 18.4102f)
                    curveTo(14.8201f, 18.1882f, 14.8201f, 17.802f, 14.5742f, 17.5801f)
                    lineTo(13.6269f, 16.7246f)
                    curveTo(13.405f, 16.5241f, 13.3189f, 16.208f, 13.4423f, 15.9355f)
                    curveTo(13.5799f, 15.6322f, 13.7467f, 15.3445f, 13.9394f, 15.0771f)
                    curveTo(14.114f, 14.8352f, 14.4299f, 14.7531f, 14.7138f, 14.8447f)
                    lineTo(15.9267f, 15.2373f)
                    curveTo(16.2418f, 15.3393f, 16.5762f, 15.1461f, 16.6455f, 14.8223f)
                    lineTo(16.9111f, 13.5791f)
                    curveTo(16.9736f, 13.2864f, 17.2041f, 13.0539f, 17.5019f, 13.0244f)
                    curveTo(17.6644f, 13.0084f, 17.8294f, 13f, 17.996f, 13f)
                    close()
                    moveTo(17.997f, 16.5f)
                    curveTo(17.1688f, 16.5002f, 16.497f, 17.1717f, 16.497f, 18f)
                    curveTo(16.497f, 18.8283f, 17.1688f, 19.4998f, 17.997f, 19.5f)
                    curveTo(18.8253f, 19.4999f, 19.497f, 18.8283f, 19.497f, 18f)
                    curveTo(19.497f, 17.1717f, 18.8253f, 16.5001f, 17.997f, 16.5f)
                    close()
                    moveTo(9.00776f, 1.49805f)
                    curveTo(9.34744f, 1.49805f, 9.68092f, 1.5215f, 10.0068f, 1.56543f)
                    lineTo(10.33f, 1.61621f)
                    lineTo(10.4394f, 1.64453f)
                    curveTo(10.6889f, 1.72923f, 10.8786f, 1.94057f, 10.9326f, 2.2041f)
                    lineTo(11.2978f, 3.98633f)
                    lineTo(11.3173f, 4.06055f)
                    curveTo(11.4334f, 4.42209f, 11.8259f, 4.6271f, 12.1953f, 4.50391f)
                    lineTo(13.9228f, 3.92773f)
                    lineTo(14.0322f, 3.89941f)
                    curveTo(14.2913f, 3.85452f, 14.5588f, 3.94938f, 14.7324f, 4.1543f)
                    curveTo(15.3037f, 4.82895f, 15.7593f, 5.60642f, 16.0654f, 6.45605f)
                    curveTo(16.1692f, 6.74465f, 16.0868f, 7.06795f, 15.8574f, 7.27148f)
                    lineTo(14.4921f, 8.48242f)
                    curveTo(14.1819f, 8.75784f, 14.1809f, 9.24216f, 14.4912f, 9.51758f)
                    lineTo(15.8554f, 10.7266f)
                    curveTo(16.0852f, 10.9302f, 16.1676f, 11.2541f, 16.0634f, 11.543f)
                    curveTo(15.7567f, 12.3932f, 15.3011f, 13.1705f, 14.7294f, 13.8447f)
                    curveTo(14.5313f, 14.0784f, 14.2105f, 14.1681f, 13.9199f, 14.0713f)
                    lineTo(12.1953f, 13.4961f)
                    curveTo(11.8259f, 13.3729f, 11.4334f, 13.5779f, 11.3173f, 13.9395f)
                    lineTo(11.2978f, 14.0137f)
                    lineTo(10.9335f, 15.791f)
                    curveTo(10.8719f, 16.0919f, 10.6333f, 16.3249f, 10.331f, 16.3789f)
                    curveTo(9.90415f, 16.455f, 9.46189f, 16.498f, 9.00776f, 16.498f)
                    curveTo(8.54894f, 16.498f, 8.10158f, 16.455f, 7.6689f, 16.377f)
                    curveTo(7.36708f, 16.3225f, 7.12886f, 16.0895f, 7.06733f, 15.7891f)
                    lineTo(6.70503f, 14.0137f)
                    curveTo(6.62182f, 13.6068f, 6.20147f, 13.3648f, 5.80757f, 13.4961f)
                    lineTo(4.09175f, 14.0674f)
                    curveTo(3.80091f, 14.1642f, 3.48022f, 14.0738f, 3.28218f, 13.8398f)
                    curveTo(2.71019f, 13.1641f, 2.25332f, 12.3856f, 1.94722f, 11.5332f)
                    curveTo(1.84354f, 11.2444f, 1.92649f, 10.9212f, 2.1562f, 10.7178f)
                    lineTo(3.51167f, 9.51758f)
                    curveTo(3.82185f, 9.24217f, 3.82185f, 8.75783f, 3.51167f, 8.48242f)
                    lineTo(2.15522f, 7.28027f)
                    curveTo(1.92584f, 7.07698f, 1.84288f, 6.75435f, 1.94624f, 6.46582f)
                    curveTo(2.25177f, 5.61402f, 2.70752f, 4.83436f, 3.27925f, 4.1582f)
                    lineTo(3.35835f, 4.07715f)
                    curveTo(3.55642f, 3.90432f, 3.83417f, 3.84578f, 4.08882f, 3.93066f)
                    lineTo(5.80757f, 4.50391f)
                    curveTo(6.20147f, 4.63524f, 6.62182f, 4.39315f, 6.70503f, 3.98633f)
                    lineTo(7.06831f, 2.20605f)
                    lineTo(7.09858f, 2.09668f)
                    curveTo(7.18952f, 1.85003f, 7.40568f, 1.66578f, 7.66987f, 1.61816f)
                    curveTo(8.10319f, 1.54008f, 8.55029f, 1.49805f, 9.00776f, 1.49805f)
                    close()
                    moveTo(9.00776f, 2.99805f)
                    curveTo(8.81325f, 2.99805f, 8.62075f, 3.00795f, 8.43061f, 3.02637f)
                    lineTo(8.17378f, 4.28711f)
                    curveTo(7.91021f, 5.57523f, 6.58031f, 6.34363f, 5.33296f, 5.92773f)
                    lineTo(4.11714f, 5.52148f)
                    curveTo(3.89685f, 5.83057f, 3.7059f, 6.16153f, 3.54683f, 6.50977f)
                    lineTo(4.50581f, 7.35938f)
                    curveTo(5.4898f, 8.2317f, 5.4898f, 9.7683f, 4.50581f, 10.6406f)
                    lineTo(3.5478f, 11.4883f)
                    curveTo(3.7071f, 11.8364f, 3.89941f, 12.1665f, 4.12007f, 12.4756f)
                    lineTo(5.33296f, 12.0723f)
                    curveTo(6.58031f, 11.6564f, 7.91021f, 12.4248f, 8.17378f, 13.7129f)
                    lineTo(8.42964f, 14.9678f)
                    curveTo(8.62039f, 14.9865f, 8.81311f, 14.998f, 9.00776f, 14.998f)
                    curveTo(9.19719f, 14.998f, 9.38506f, 14.9877f, 9.57124f, 14.9697f)
                    lineTo(9.82905f, 13.7129f)
                    lineTo(9.89058f, 13.4775f)
                    curveTo(10.2585f, 12.3334f, 11.5005f, 11.6823f, 12.6699f, 12.0723f)
                    lineTo(13.8916f, 12.4795f)
                    curveTo(14.1116f, 12.1717f, 14.3026f, 11.8427f, 14.4619f, 11.4961f)
                    lineTo(13.497f, 10.6406f)
                    curveTo(12.513f, 9.76834f, 12.5122f, 8.23173f, 13.496f, 7.35938f)
                    lineTo(14.4628f, 6.50098f)
                    curveTo(14.3038f, 6.15434f, 14.1132f, 5.82522f, 13.8935f, 5.51758f)
                    lineTo(12.6699f, 5.92676f)
                    curveTo(11.5005f, 6.31668f, 10.2585f, 5.66665f, 9.89058f, 4.52246f)
                    lineTo(9.82905f, 4.28711f)
                    lineTo(9.57026f, 3.02539f)
                    curveTo(9.38467f, 3.00769f, 9.19707f, 2.99805f, 9.00776f, 2.99805f)
                    close()
                    moveTo(8.99995f, 6.24707f)
                    curveTo(10.5187f, 6.2471f, 11.75f, 7.4783f, 11.75f, 8.99707f)
                    curveTo(11.75f, 10.5158f, 10.5187f, 11.747f, 8.99995f, 11.7471f)
                    curveTo(7.48117f, 11.7471f, 6.24995f, 10.5159f, 6.24995f, 8.99707f)
                    curveTo(6.24995f, 7.47829f, 7.48117f, 6.24707f, 8.99995f, 6.24707f)
                    close()
                    moveTo(8.99995f, 7.74707f)
                    curveTo(8.30959f, 7.74707f, 7.74995f, 8.30671f, 7.74995f, 8.99707f)
                    curveTo(7.74995f, 9.68743f, 8.30959f, 10.2471f, 8.99995f, 10.2471f)
                    curveTo(9.69028f, 10.247f, 10.25f, 9.68741f, 10.25f, 8.99707f)
                    curveTo(10.25f, 8.30673f, 9.69028f, 7.7471f, 8.99995f, 7.74707f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsSettingsCogMultiple!!
        }

    private var _FluentuiSystemIconsSettingsCogMultiple: ImageVector? = null

    val TablerCirclesRelation: ImageVector
        get() {
            if (_TablerCirclesRelation != null) return _TablerCirclesRelation!!

            _TablerCirclesRelation = ImageVector.Builder(
                name = "circles-relation",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(9.183f, 6.117f)
                    arcToRelative(6f, 6f, 0f, true, false, 4.511f, 3.986f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(14.813f, 17.883f)
                    arcToRelative(6f, 6f, 0f, true, false, -4.496f, -3.954f)
                }
            }.build()

            return _TablerCirclesRelation!!
        }

    private var _TablerCirclesRelation: ImageVector? = null

    val MaterialSymbolsCircles: ImageVector
        get() {
            if (_MaterialSymbolsCircles != null) return _MaterialSymbolsCircles!!

            _MaterialSymbolsCircles = ImageVector.Builder(
                name = "circles",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(666f, 600f)
                    quadToRelative(33f, 2f, 65.5f, -3.5f)
                    reflectiveQuadTo(794f, 578f)
                    quadToRelative(-21f, 131f, -121f, 216.5f)
                    reflectiveQuadTo(440f, 880f)
                    quadToRelative(-75f, 0f, -140.5f, -28.5f)
                    reflectiveQuadToRelative(-114f, -77f)
                    quadToRelative(-48.5f, -48.5f, -77f, -114f)
                    reflectiveQuadTo(80f, 520f)
                    quadToRelative(0f, -133f, 85.5f, -233f)
                    reflectiveQuadTo(382f, 166f)
                    quadToRelative(-13f, 30f, -18.5f, 62.5f)
                    reflectiveQuadTo(360f, 294f)
                    quadToRelative(-72f, 25f, -116f, 87f)
                    reflectiveQuadToRelative(-44f, 139f)
                    quadToRelative(0f, 100f, 70f, 170f)
                    reflectiveQuadToRelative(170f, 70f)
                    quadToRelative(77f, 0f, 139f, -44f)
                    reflectiveQuadToRelative(87f, -116f)
                    close()
                    moveToRelative(14f, -560f)
                    quadToRelative(100f, 0f, 170f, 70f)
                    reflectiveQuadToRelative(70f, 170f)
                    quadToRelative(0f, 100f, -70f, 170f)
                    reflectiveQuadToRelative(-170f, 70f)
                    quadToRelative(-100f, 0f, -170f, -70f)
                    reflectiveQuadToRelative(-70f, -170f)
                    quadToRelative(0f, -100f, 70f, -170f)
                    reflectiveQuadToRelative(170f, -70f)
                    close()
                    moveToRelative(0f, 360f)
                    quadToRelative(50f, 0f, 85f, -35f)
                    reflectiveQuadToRelative(35f, -85f)
                    quadToRelative(0f, -50f, -35f, -85f)
                    reflectiveQuadToRelative(-85f, -35f)
                    quadToRelative(-50f, 0f, -85f, 35f)
                    reflectiveQuadToRelative(-35f, 85f)
                    quadToRelative(0f, 50f, 35f, 85f)
                    reflectiveQuadToRelative(85f, 35f)
                    close()
                    moveToRelative(0f, -120f)
                    close()
                    moveTo(433f, 527f)
                    close()
                }
            }.build()

            return _MaterialSymbolsCircles!!
        }

    private var _MaterialSymbolsCircles: ImageVector? = null

    val TablerClearAll: ImageVector
        get() {
            if (_TablerClearAll != null) return _TablerClearAll!!

            _TablerClearAll = ImageVector.Builder(
                name = "clear-all",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(8f, 6f)
                    horizontalLineToRelative(12f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(6f, 12f)
                    horizontalLineToRelative(12f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(4f, 18f)
                    horizontalLineToRelative(12f)
                }
            }.build()

            return _TablerClearAll!!
        }

    private var _TablerClearAll: ImageVector? = null

    val FluentuiSystemIconsApps: ImageVector
        get() {
            if (_FluentuiSystemIconsApps != null) return _FluentuiSystemIconsApps!!

            _FluentuiSystemIconsApps = ImageVector.Builder(
                name = "apps",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(18.4923f, 2.33088f)
                    lineTo(21.671f, 5.50966f)
                    curveTo(22.5497f, 6.38834f, 22.5497f, 7.81296f, 21.671f, 8.69164f)
                    lineTo(19.0866f, 11.2756f)
                    curveTo(20.1696f, 11.438f, 21f, 12.3723f, 21f, 13.5006f)
                    verticalLineTo(18.7506f)
                    curveTo(21f, 19.9932f, 19.9926f, 21.0006f, 18.75f, 21.0006f)
                    horizontalLineTo(5.25f)
                    curveTo(4.00736f, 21.0006f, 3f, 19.9932f, 3f, 18.7506f)
                    verticalLineTo(5.25055f)
                    curveTo(3f, 4.00791f, 4.00736f, 3.00055f, 5.25f, 3.00055f)
                    horizontalLineTo(10.5f)
                    curveTo(11.6289f, 3.00055f, 12.5637f, 3.83201f, 12.7253f, 4.91596f)
                    lineTo(15.3103f, 2.33088f)
                    curveTo(16.189f, 1.45221f, 17.6136f, 1.45221f, 18.4923f, 2.33088f)
                    close()
                    moveTo(4.5f, 18.7506f)
                    curveTo(4.5f, 19.1648f, 4.83579f, 19.5006f, 5.25f, 19.5006f)
                    lineTo(11.249f, 19.4999f)
                    lineTo(11.25f, 12.7506f)
                    lineTo(4.5f, 12.7499f)
                    verticalLineTo(18.7506f)
                    close()
                    moveTo(12.749f, 19.4999f)
                    lineTo(18.75f, 19.5006f)
                    curveTo(19.1642f, 19.5006f, 19.5f, 19.1648f, 19.5f, 18.7506f)
                    verticalLineTo(13.5006f)
                    curveTo(19.5f, 13.0863f, 19.1642f, 12.7506f, 18.75f, 12.7506f)
                    lineTo(12.749f, 12.7499f)
                    verticalLineTo(19.4999f)
                    close()
                    moveTo(10.5f, 4.50055f)
                    horizontalLineTo(5.25f)
                    curveTo(4.83579f, 4.50055f, 4.5f, 4.83634f, 4.5f, 5.25055f)
                    verticalLineTo(11.2499f)
                    horizontalLineTo(11.25f)
                    verticalLineTo(5.25055f)
                    curveTo(11.25f, 4.83634f, 10.9142f, 4.50055f, 10.5f, 4.50055f)
                    close()
                    moveTo(12.75f, 9.30988f)
                    verticalLineTo(11.2506f)
                    lineTo(14.69f, 11.2499f)
                    lineTo(12.75f, 9.30988f)
                    close()
                    moveTo(16.3709f, 3.39154f)
                    lineTo(13.1922f, 6.57032f)
                    curveTo(12.8993f, 6.86321f, 12.8993f, 7.33808f, 13.1922f, 7.63098f)
                    lineTo(16.3709f, 10.8097f)
                    curveTo(16.6638f, 11.1026f, 17.1387f, 11.1026f, 17.4316f, 10.8097f)
                    lineTo(20.6104f, 7.63098f)
                    curveTo(20.9033f, 7.33808f, 20.9033f, 6.86321f, 20.6104f, 6.57032f)
                    lineTo(17.4316f, 3.39154f)
                    curveTo(17.1387f, 3.09865f, 16.6638f, 3.09865f, 16.3709f, 3.39154f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsApps!!
        }

    private var _FluentuiSystemIconsApps: ImageVector? = null

    val MaterialIconsPause: ImageVector
        get() {
            if (_MaterialIconsPause != null) return _MaterialIconsPause!!

            _MaterialIconsPause = ImageVector.Builder(
                name = "pause",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(8f, 19f)
                    curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                    verticalLineTo(7f)
                    curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                    reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
                    verticalLineToRelative(10f)
                    curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                    close()
                    moveToRelative(6f, -12f)
                    verticalLineToRelative(10f)
                    curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                    reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                    verticalLineTo(7f)
                    curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                    reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
                    close()
                }
            }.build()

            return _MaterialIconsPause!!
        }

    private var _MaterialIconsPause: ImageVector? = null

    val VscodeCodiconsTriangleRight: ImageVector
        get() {
            if (_VscodeCodiconsTriangleRight != null) return _VscodeCodiconsTriangleRight!!

            _VscodeCodiconsTriangleRight = ImageVector.Builder(
                name = "triangle-right",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(7.57107f, 11.8403f)
                    curveTo(6.90803f, 12.2987f, 6f, 11.8272f, 6f, 11.0244f)
                    verticalLineTo(4.9756f)
                    curveTo(6f, 4.17287f, 6.90803f, 3.70133f, 7.57106f, 4.15974f)
                    lineTo(11.3555f, 6.77622f)
                    curveTo(12.2133f, 7.36933f, 12.2134f, 8.6307f, 11.3555f, 9.22382f)
                    lineTo(7.57107f, 11.8403f)
                    close()
                }
            }.build()

            return _VscodeCodiconsTriangleRight!!
        }

    private var _VscodeCodiconsTriangleRight: ImageVector? = null

    val TablerReload: ImageVector
        get() {
            if (_TablerReload != null) return _TablerReload!!

            _TablerReload = ImageVector.Builder(
                name = "reload",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(19.933f, 13.041f)
                    arcToRelative(8f, 8f, 0f, true, true, -9.925f, -8.788f)
                    curveToRelative(3.899f, -1f, 7.935f, 1.007f, 9.425f, 4.747f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(20f, 4f)
                    verticalLineToRelative(5f)
                    horizontalLineToRelative(-5f)
                }
            }.build()

            return _TablerReload!!
        }

    private var _TablerReload: ImageVector? = null

    val FeatherTrash: ImageVector
        get() {
            if (_FeatherTrash != null) return _FeatherTrash!!

            _FeatherTrash = ImageVector.Builder(
                name = "trash",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(3f, 6f)
                    lineTo(5f, 6f)
                    lineTo(21f, 6f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(19f, 6f)
                    verticalLineToRelative(14f)
                    arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                    horizontalLineTo(7f)
                    arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                    verticalLineTo(6f)
                    moveToRelative(3f, 0f)
                    verticalLineTo(4f)
                    arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                    horizontalLineToRelative(4f)
                    arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                    verticalLineToRelative(2f)
                }
            }.build()

            return _FeatherTrash!!
        }

    private var _FeatherTrash: ImageVector? = null

    val FeatherChevronUp: ImageVector
        get() {
            if (_FeatherChevronUp != null) return _FeatherChevronUp!!

            _FeatherChevronUp = ImageVector.Builder(
                name = "chevron-up",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(18f, 15f)
                    lineTo(12f, 9f)
                    lineTo(6f, 15f)
                }
            }.build()

            return _FeatherChevronUp!!
        }

    private var _FeatherChevronUp: ImageVector? = null

    val FeatherChevronDown: ImageVector
        get() {
            if (_FeatherChevronDown != null) return _FeatherChevronDown!!

            _FeatherChevronDown = ImageVector.Builder(
                name = "chevron-down",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(6f, 9f)
                    lineTo(12f, 15f)
                    lineTo(18f, 9f)
                }
            }.build()

            return _FeatherChevronDown!!
        }

    private var _FeatherChevronDown: ImageVector? = null

    val FluentuiSystemIconsSineWaveDots: ImageVector
        get() {
            if (_FluentuiSystemIconsSineWaveDots != null) return _FluentuiSystemIconsSineWaveDots!!

            _FluentuiSystemIconsSineWaveDots = ImageVector.Builder(
                name = "sine-wave-dots",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(8.6084f, 1.97949f)
                    curveTo(9.45527f, 1.97952f, 10.0793f, 2.48271f, 10.5156f, 3.09961f)
                    curveTo(10.9464f, 3.7088f, 11.2716f, 4.52759f, 11.5352f, 5.42285f)
                    curveTo(12.0641f, 7.2194f, 12.4186f, 9.59186f, 12.7627f, 11.8857f)
                    curveTo(13.1124f, 14.2168f, 13.452f, 16.4694f, 13.9443f, 18.1416f)
                    curveTo(14.1914f, 18.9805f, 14.4586f, 19.6073f, 14.7432f, 20.0098f)
                    curveTo(15.0222f, 20.4044f, 15.2365f, 20.4795f, 15.3916f, 20.4795f)
                    curveTo(15.5309f, 20.4795f, 15.7048f, 20.4217f, 15.9316f, 20.1514f)
                    curveTo(16.1702f, 19.8671f, 16.4091f, 19.4097f, 16.6377f, 18.7666f)
                    curveTo(17.0935f, 17.4842f, 17.428f, 15.7004f, 17.7393f, 13.7295f)
                    curveTo(17.7398f, 13.7263f, 17.7397f, 13.7229f, 17.7402f, 13.7197f)
                    curveTo(16.7135f, 13.2433f, 16f, 12.2064f, 16f, 11f)
                    curveTo(16f, 9.34315f, 17.3431f, 8f, 19f, 8f)
                    curveTo(20.6569f, 8f, 22f, 9.34315f, 22f, 11f)
                    curveTo(22f, 12.5693f, 20.7949f, 13.8556f, 19.2598f, 13.9873f)
                    curveTo(18.9506f, 15.943f, 18.5991f, 17.8529f, 18.0908f, 19.2832f)
                    curveTo(17.8366f, 19.9984f, 17.5224f, 20.6539f, 17.1123f, 21.1426f)
                    curveTo(16.6906f, 21.645f, 16.1197f, 22.0205f, 15.3916f, 22.0205f)
                    curveTo(14.5447f, 22.0205f, 13.9207f, 21.5173f, 13.4844f, 20.9004f)
                    curveTo(13.0536f, 20.2912f, 12.7285f, 19.4724f, 12.4648f, 18.5771f)
                    curveTo(11.9359f, 16.7806f, 11.5814f, 14.4081f, 11.2373f, 12.1143f)
                    curveTo(10.8876f, 9.7832f, 10.548f, 7.53056f, 10.0557f, 5.8584f)
                    curveTo(9.80864f, 5.01946f, 9.54142f, 4.39275f, 9.25684f, 3.99023f)
                    curveTo(8.97775f, 3.59557f, 8.76349f, 3.52053f, 8.6084f, 3.52051f)
                    curveTo(8.46908f, 3.52051f, 8.29523f, 3.57829f, 8.06836f, 3.84863f)
                    curveTo(7.82983f, 4.13291f, 7.59087f, 4.59029f, 7.3623f, 5.2334f)
                    curveTo(6.90655f, 6.5158f, 6.57203f, 8.2996f, 6.26074f, 10.2705f)
                    curveTo(6.26027f, 10.2735f, 6.25929f, 10.2763f, 6.25879f, 10.2793f)
                    curveTo(5.0484f, 8.05629f, 5.40093f, 6.1469f, 5.90918f, 4.7168f)
                    curveTo(6.16337f, 4.0016f, 6.47764f, 3.34612f, 6.8877f, 2.85742f)
                    curveTo(7.30939f, 2.35502f, 7.88027f, 1.97949f, 8.6084f, 1.97949f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsSineWaveDots!!
        }

    private var _FluentuiSystemIconsSineWaveDots: ImageVector? = null

    val TablerBrandSpeedtest: ImageVector
        get() {
            if (_TablerBrandSpeedtest != null) return _TablerBrandSpeedtest!!

            _TablerBrandSpeedtest = ImageVector.Builder(
                name = "brand-speedtest",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(5.636f, 19.364f)
                    arcToRelative(9f, 9f, 0f, true, true, 12.728f, 0f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(16f, 9f)
                    lineToRelative(-4f, 4f)
                }
            }.build()

            return _TablerBrandSpeedtest!!
        }

    private var _TablerBrandSpeedtest: ImageVector? = null

    val BootstrapStars: ImageVector
        get() {
            if (_BootstrapStars != null) return _BootstrapStars!!

            _BootstrapStars = ImageVector.Builder(
                name = "stars",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(7.657f, 6.247f)
                    curveToRelative(0.11f, -0.33f, 0.576f, -0.33f, 0.686f, 0f)
                    lineToRelative(0.645f, 1.937f)
                    arcToRelative(2.89f, 2.89f, 0f, false, false, 1.829f, 1.828f)
                    lineToRelative(1.936f, 0.645f)
                    curveToRelative(0.33f, 0.11f, 0.33f, 0.576f, 0f, 0.686f)
                    lineToRelative(-1.937f, 0.645f)
                    arcToRelative(2.89f, 2.89f, 0f, false, false, -1.828f, 1.829f)
                    lineToRelative(-0.645f, 1.936f)
                    arcToRelative(0.361f, 0.361f, 0f, false, true, -0.686f, 0f)
                    lineToRelative(-0.645f, -1.937f)
                    arcToRelative(2.89f, 2.89f, 0f, false, false, -1.828f, -1.828f)
                    lineToRelative(-1.937f, -0.645f)
                    arcToRelative(0.361f, 0.361f, 0f, false, true, 0f, -0.686f)
                    lineToRelative(1.937f, -0.645f)
                    arcToRelative(2.89f, 2.89f, 0f, false, false, 1.828f, -1.828f)
                    close()
                    moveTo(3.794f, 1.148f)
                    arcToRelative(0.217f, 0.217f, 0f, false, true, 0.412f, 0f)
                    lineToRelative(0.387f, 1.162f)
                    curveToRelative(0.173f, 0.518f, 0.579f, 0.924f, 1.097f, 1.097f)
                    lineToRelative(1.162f, 0.387f)
                    arcToRelative(0.217f, 0.217f, 0f, false, true, 0f, 0.412f)
                    lineToRelative(-1.162f, 0.387f)
                    arcTo(1.73f, 1.73f, 0f, false, false, 4.593f, 5.69f)
                    lineToRelative(-0.387f, 1.162f)
                    arcToRelative(0.217f, 0.217f, 0f, false, true, -0.412f, 0f)
                    lineTo(3.407f, 5.69f)
                    arcTo(1.73f, 1.73f, 0f, false, false, 2.31f, 4.593f)
                    lineToRelative(-1.162f, -0.387f)
                    arcToRelative(0.217f, 0.217f, 0f, false, true, 0f, -0.412f)
                    lineToRelative(1.162f, -0.387f)
                    arcTo(1.73f, 1.73f, 0f, false, false, 3.407f, 2.31f)
                    close()
                    moveTo(10.863f, 0.099f)
                    arcToRelative(0.145f, 0.145f, 0f, false, true, 0.274f, 0f)
                    lineToRelative(0.258f, 0.774f)
                    curveToRelative(0.115f, 0.346f, 0.386f, 0.617f, 0.732f, 0.732f)
                    lineToRelative(0.774f, 0.258f)
                    arcToRelative(0.145f, 0.145f, 0f, false, true, 0f, 0.274f)
                    lineToRelative(-0.774f, 0.258f)
                    arcToRelative(1.16f, 1.16f, 0f, false, false, -0.732f, 0.732f)
                    lineToRelative(-0.258f, 0.774f)
                    arcToRelative(0.145f, 0.145f, 0f, false, true, -0.274f, 0f)
                    lineToRelative(-0.258f, -0.774f)
                    arcToRelative(1.16f, 1.16f, 0f, false, false, -0.732f, -0.732f)
                    lineTo(9.1f, 2.137f)
                    arcToRelative(0.145f, 0.145f, 0f, false, true, 0f, -0.274f)
                    lineToRelative(0.774f, -0.258f)
                    curveToRelative(0.346f, -0.115f, 0.617f, -0.386f, 0.732f, -0.732f)
                    close()
                }
            }.build()

            return _BootstrapStars!!
        }

    private var _BootstrapStars: ImageVector? = null

    val BubbleMultiple: ImageVector
        get() {
            if (_FluentuiSystemIconsBubbleMultiple != null) return _FluentuiSystemIconsBubbleMultiple!!

            _FluentuiSystemIconsBubbleMultiple = ImageVector.Builder(
                name = "bubble-multiple",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 20f,
                viewportHeight = 20f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(15.5f, 2f)
                    curveTo(14.1193f, 2f, 13f, 3.11929f, 13f, 4.5f)
                    curveTo(13f, 5.88071f, 14.1193f, 7f, 15.5f, 7f)
                    curveTo(16.8807f, 7f, 18f, 5.88071f, 18f, 4.5f)
                    curveTo(18f, 3.11929f, 16.8807f, 2f, 15.5f, 2f)
                    close()
                    moveTo(12f, 4.5f)
                    curveTo(12f, 2.567f, 13.567f, 1f, 15.5f, 1f)
                    curveTo(17.433f, 1f, 19f, 2.567f, 19f, 4.5f)
                    curveTo(19f, 6.433f, 17.433f, 8f, 15.5f, 8f)
                    curveTo(13.567f, 8f, 12f, 6.433f, 12f, 4.5f)
                    close()
                    moveTo(3f, 10f)
                    curveTo(3f, 7.79086f, 4.79086f, 6f, 7f, 6f)
                    curveTo(9.20914f, 6f, 11f, 7.79086f, 11f, 10f)
                    curveTo(11f, 12.2091f, 9.20914f, 14f, 7f, 14f)
                    curveTo(4.79086f, 14f, 3f, 12.2091f, 3f, 10f)
                    close()
                    moveTo(7f, 5f)
                    curveTo(4.23858f, 5f, 2f, 7.23858f, 2f, 10f)
                    curveTo(2f, 12.7614f, 4.23858f, 15f, 7f, 15f)
                    curveTo(9.76142f, 15f, 12f, 12.7614f, 12f, 10f)
                    curveTo(12f, 7.23858f, 9.76142f, 5f, 7f, 5f)
                    close()
                    moveTo(12.4989f, 17.9989f)
                    curveTo(10.9484f, 17.9989f, 9.63342f, 16.9907f, 9.17368f, 15.5941f)
                    curveTo(9.49109f, 15.4707f, 9.7954f, 15.3211f, 10.0839f, 15.1478f)
                    curveTo(10.3697f, 16.2139f, 11.3426f, 16.9989f, 12.4989f, 16.9989f)
                    curveTo(13.8796f, 16.9989f, 14.9989f, 15.8796f, 14.9989f, 14.4989f)
                    curveTo(14.9989f, 13.1713f, 13.9642f, 12.0855f, 12.6572f, 12.0038f)
                    curveTo(12.769f, 11.6882f, 12.8552f, 11.3605f, 12.9131f, 11.0231f)
                    curveTo(14.6509f, 11.228f, 15.9989f, 12.706f, 15.9989f, 14.4989f)
                    curveTo(15.9989f, 16.4319f, 14.4319f, 17.9989f, 12.4989f, 17.9989f)
                    close()
                    moveTo(7.66662f, 7.20046f)
                    curveTo(7.40626f, 7.10843f, 7.1206f, 7.24489f, 7.02858f, 7.50525f)
                    curveTo(6.93656f, 7.76561f, 7.07302f, 8.05127f, 7.33338f, 8.14329f)
                    curveTo(8.04309f, 8.39414f, 8.6065f, 8.95737f, 8.85759f, 9.66698f)
                    curveTo(8.9497f, 9.92731f, 9.23541f, 10.0637f, 9.49574f, 9.97155f)
                    curveTo(9.75606f, 9.87944f, 9.89242f, 9.59373f, 9.80031f, 9.33341f)
                    curveTo(9.44849f, 8.33911f, 8.66108f, 7.55195f, 7.66662f, 7.20046f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsBubbleMultiple!!
        }

    private var _FluentuiSystemIconsBubbleMultiple: ImageVector? = null

    val BootstrapFiletypeSvg: ImageVector
        get() {
            if (_BootstrapFiletypeSvg != null) return _BootstrapFiletypeSvg!!

            _BootstrapFiletypeSvg = ImageVector.Builder(
                name = "filetype-svg",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(14f, 4.5f)
                    verticalLineTo(14f)
                    arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                    verticalLineToRelative(-1f)
                    arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                    verticalLineTo(4.5f)
                    horizontalLineToRelative(-2f)
                    arcTo(1.5f, 1.5f, 0f, false, true, 9.5f, 3f)
                    verticalLineTo(1f)
                    horizontalLineTo(4f)
                    arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                    verticalLineToRelative(9f)
                    horizontalLineTo(2f)
                    verticalLineTo(2f)
                    arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                    horizontalLineToRelative(5.5f)
                    close()
                    moveTo(0f, 14.841f)
                    arcToRelative(1.13f, 1.13f, 0f, false, false, 0.401f, 0.823f)
                    quadToRelative(0.194f, 0.162f, 0.478f, 0.252f)
                    quadToRelative(0.285f, 0.091f, 0.665f, 0.091f)
                    quadToRelative(0.507f, 0f, 0.858f, -0.158f)
                    quadToRelative(0.355f, -0.158f, 0.54f, -0.44f)
                    arcToRelative(1.17f, 1.17f, 0f, false, false, 0.187f, -0.656f)
                    quadToRelative(0f, -0.336f, -0.135f, -0.56f)
                    arcToRelative(1f, 1f, 0f, false, false, -0.375f, -0.357f)
                    arcToRelative(2f, 2f, 0f, false, false, -0.565f, -0.21f)
                    lineToRelative(-0.621f, -0.144f)
                    arcToRelative(1f, 1f, 0f, false, true, -0.405f, -0.176f)
                    arcToRelative(0.37f, 0.37f, 0f, false, true, -0.143f, -0.299f)
                    quadToRelative(0f, -0.234f, 0.184f, -0.384f)
                    quadToRelative(0.187f, -0.152f, 0.513f, -0.152f)
                    quadToRelative(0.214f, 0f, 0.37f, 0.068f)
                    arcToRelative(0.6f, 0.6f, 0f, false, true, 0.245f, 0.181f)
                    arcToRelative(0.56f, 0.56f, 0f, false, true, 0.12f, 0.258f)
                    horizontalLineToRelative(0.75f)
                    arcToRelative(1.1f, 1.1f, 0f, false, false, -0.199f, -0.566f)
                    arcToRelative(1.2f, 1.2f, 0f, false, false, -0.5f, -0.41f)
                    arcToRelative(1.8f, 1.8f, 0f, false, false, -0.78f, -0.152f)
                    quadToRelative(-0.44f, 0f, -0.776f, 0.15f)
                    quadToRelative(-0.337f, 0.149f, -0.528f, 0.421f)
                    quadToRelative(-0.19f, 0.273f, -0.19f, 0.639f)
                    quadToRelative(0f, 0.302f, 0.123f, 0.524f)
                    reflectiveQuadToRelative(0.351f, 0.367f)
                    quadToRelative(0.229f, 0.143f, 0.54f, 0.213f)
                    lineToRelative(0.618f, 0.144f)
                    quadToRelative(0.31f, 0.073f, 0.462f, 0.193f)
                    arcToRelative(0.39f, 0.39f, 0f, false, true, 0.153f, 0.326f)
                    arcToRelative(0.5f, 0.5f, 0f, false, true, -0.085f, 0.29f)
                    arcToRelative(0.56f, 0.56f, 0f, false, true, -0.256f, 0.193f)
                    quadToRelative(-0.167f, 0.07f, -0.413f, 0.07f)
                    quadToRelative(-0.176f, 0f, -0.32f, -0.04f)
                    arcToRelative(0.8f, 0.8f, 0f, false, true, -0.248f, -0.115f)
                    arcToRelative(0.58f, 0.58f, 0f, false, true, -0.255f, -0.384f)
                    close()
                    moveToRelative(4.575f, 1.09f)
                    horizontalLineToRelative(0.952f)
                    lineToRelative(1.327f, -3.999f)
                    horizontalLineToRelative(-0.879f)
                    lineToRelative(-0.887f, 3.138f)
                    horizontalLineTo(5.05f)
                    lineToRelative(-0.897f, -3.138f)
                    horizontalLineToRelative(-0.917f)
                    close()
                    moveToRelative(5.483f, -3.293f)
                    quadToRelative(0.114f, 0.228f, 0.14f, 0.492f)
                    horizontalLineToRelative(-0.776f)
                    arcToRelative(0.8f, 0.8f, 0f, false, false, -0.096f, -0.249f)
                    arcToRelative(0.7f, 0.7f, 0f, false, false, -0.17f, -0.19f)
                    arcToRelative(0.7f, 0.7f, 0f, false, false, -0.237f, -0.126f)
                    arcToRelative(1f, 1f, 0f, false, false, -0.3f, -0.044f)
                    quadToRelative(-0.427f, 0f, -0.664f, 0.302f)
                    quadToRelative(-0.235f, 0.3f, -0.235f, 0.85f)
                    verticalLineToRelative(0.497f)
                    quadToRelative(0f, 0.352f, 0.097f, 0.616f)
                    arcToRelative(0.9f, 0.9f, 0f, false, false, 0.305f, 0.413f)
                    arcToRelative(0.87f, 0.87f, 0f, false, false, 0.518f, 0.146f)
                    arcToRelative(1f, 1f, 0f, false, false, 0.457f, -0.097f)
                    arcToRelative(0.67f, 0.67f, 0f, false, false, 0.273f, -0.263f)
                    quadToRelative(0.09f, -0.164f, 0.09f, -0.364f)
                    verticalLineToRelative(-0.254f)
                    horizontalLineToRelative(-0.823f)
                    verticalLineToRelative(-0.59f)
                    horizontalLineToRelative(1.576f)
                    verticalLineToRelative(0.798f)
                    quadToRelative(0f, 0.29f, -0.096f, 0.55f)
                    arcToRelative(1.3f, 1.3f, 0f, false, true, -0.293f, 0.457f)
                    arcToRelative(1.4f, 1.4f, 0f, false, true, -0.495f, 0.314f)
                    quadToRelative(-0.296f, 0.111f, -0.698f, 0.111f)
                    arcToRelative(2f, 2f, 0f, false, true, -0.752f, -0.132f)
                    arcToRelative(1.45f, 1.45f, 0f, false, true, -0.534f, -0.377f)
                    arcToRelative(1.6f, 1.6f, 0f, false, true, -0.319f, -0.58f)
                    arcToRelative(2.5f, 2.5f, 0f, false, true, -0.105f, -0.745f)
                    verticalLineToRelative(-0.507f)
                    quadToRelative(0f, -0.54f, 0.199f, -0.949f)
                    quadToRelative(0.202f, -0.406f, 0.583f, -0.633f)
                    quadToRelative(0.383f, -0.228f, 0.926f, -0.228f)
                    quadToRelative(0.357f, 0f, 0.635f, 0.1f)
                    quadToRelative(0.282f, 0.1f, 0.48f, 0.275f)
                    quadToRelative(0.2f, 0.176f, 0.314f, 0.407f)
                }
            }.build()

            return _BootstrapFiletypeSvg!!
        }

    private var _BootstrapFiletypeSvg: ImageVector? = null

    val FluentuiSystemIconsAppsAddIn: ImageVector
        get() {
            if (_FluentuiSystemIconsAppsAddIn != null) return _FluentuiSystemIconsAppsAddIn!!

            _FluentuiSystemIconsAppsAddIn = ImageVector.Builder(
                name = "apps-add-in",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(10.5f, 3f)
                    curveTo(11.7426f, 3f, 12.75f, 4.00736f, 12.75f, 5.25f)
                    verticalLineTo(11.25f)
                    horizontalLineTo(18.75f)
                    curveTo(19.9926f, 11.25f, 21f, 12.2574f, 21f, 13.5f)
                    verticalLineTo(18.75f)
                    curveTo(21f, 19.9926f, 19.9926f, 21f, 18.75f, 21f)
                    horizontalLineTo(5.25f)
                    curveTo(4.00736f, 21f, 3f, 19.9926f, 3f, 18.75f)
                    verticalLineTo(5.25f)
                    curveTo(3f, 4.00736f, 4.00736f, 3f, 5.25f, 3f)
                    horizontalLineTo(10.5f)
                    close()
                    moveTo(11.25f, 12.75f)
                    horizontalLineTo(4.5f)
                    verticalLineTo(18.75f)
                    curveTo(4.5f, 19.1642f, 4.83579f, 19.5f, 5.25f, 19.5f)
                    horizontalLineTo(11.249f)
                    lineTo(11.25f, 12.75f)
                    close()
                    moveTo(18.75f, 12.75f)
                    horizontalLineTo(12.749f)
                    verticalLineTo(19.5f)
                    horizontalLineTo(18.75f)
                    curveTo(19.1642f, 19.5f, 19.5f, 19.1642f, 19.5f, 18.75f)
                    verticalLineTo(13.5f)
                    curveTo(19.5f, 13.0858f, 19.1642f, 12.75f, 18.75f, 12.75f)
                    close()
                    moveTo(10.5f, 4.5f)
                    horizontalLineTo(5.25f)
                    curveTo(4.83579f, 4.5f, 4.5f, 4.83579f, 4.5f, 5.25f)
                    verticalLineTo(11.25f)
                    horizontalLineTo(11.25f)
                    verticalLineTo(5.25f)
                    curveTo(11.25f, 4.83579f, 10.9142f, 4.5f, 10.5f, 4.5f)
                    close()
                    moveTo(17.8982f, 2.00685f)
                    lineTo(18f, 2f)
                    curveTo(18.3797f, 2f, 18.6935f, 2.28215f, 18.7432f, 2.64823f)
                    lineTo(18.75f, 2.75f)
                    verticalLineTo(5.25f)
                    horizontalLineTo(21.25f)
                    curveTo(21.6297f, 5.25f, 21.9435f, 5.53215f, 21.9932f, 5.89823f)
                    lineTo(22f, 6f)
                    curveTo(22f, 6.3797f, 21.7178f, 6.69349f, 21.3518f, 6.74315f)
                    lineTo(21.25f, 6.75f)
                    horizontalLineTo(18.75f)
                    verticalLineTo(9.25f)
                    curveTo(18.75f, 9.6297f, 18.4678f, 9.94349f, 18.1018f, 9.99315f)
                    lineTo(18f, 10f)
                    curveTo(17.6203f, 10f, 17.3065f, 9.71785f, 17.2568f, 9.35177f)
                    lineTo(17.25f, 9.25f)
                    verticalLineTo(6.75f)
                    horizontalLineTo(14.75f)
                    curveTo(14.3703f, 6.75f, 14.0565f, 6.46785f, 14.0068f, 6.10177f)
                    lineTo(14f, 6f)
                    curveTo(14f, 5.6203f, 14.2822f, 5.30651f, 14.6482f, 5.25685f)
                    lineTo(14.75f, 5.25f)
                    horizontalLineTo(17.25f)
                    verticalLineTo(2.75f)
                    curveTo(17.25f, 2.3703f, 17.5322f, 2.05651f, 17.8982f, 2.00685f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsAppsAddIn!!
        }

    private var _FluentuiSystemIconsAppsAddIn: ImageVector? = null

    val HeroiconsCloud: ImageVector
        get() {
            if (_HeroiconsCloud != null) return _HeroiconsCloud!!

            _HeroiconsCloud = ImageVector.Builder(
                name = "cloud",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.5f,
                    strokeLineJoin = StrokeJoin.Miter
                ) {
                    moveTo(2.25f, 15f)
                    arcToRelative(4.5f, 4.5f, 0f, false, false, 4.5f, 4.5f)
                    horizontalLineTo(18f)
                    arcToRelative(3.75f, 3.75f, 0f, false, false, 1.332f, -7.257f)
                    arcToRelative(3f, 3f, 0f, false, false, -3.758f, -3.848f)
                    arcToRelative(5.25f, 5.25f, 0f, false, false, -10.233f, 2.33f)
                    arcTo(4.502f, 4.502f, 0f, false, false, 2.25f, 15f)
                    close()
                }
            }.build()

            return _HeroiconsCloud!!
        }

    private var _HeroiconsCloud: ImageVector? = null

}