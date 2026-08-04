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

    val FontAwesomeWaveSquare: ImageVector
        get() {
            if (_FontAwesomeWaveSquare != null) return _FontAwesomeWaveSquare!!

            _FontAwesomeWaveSquare = ImageVector.Builder(
                name = "wave-square",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 640f,
                viewportHeight = 512f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(476f, 480f)
                    horizontalLineTo(324f)
                    arcToRelative(36f, 36f, 0f, false, true, -36f, -36f)
                    verticalLineTo(96f)
                    horizontalLineToRelative(-96f)
                    verticalLineToRelative(156f)
                    arcToRelative(36f, 36f, 0f, false, true, -36f, 36f)
                    horizontalLineTo(16f)
                    arcToRelative(16f, 16f, 0f, false, true, -16f, -16f)
                    verticalLineToRelative(-32f)
                    arcToRelative(16f, 16f, 0f, false, true, 16f, -16f)
                    horizontalLineToRelative(112f)
                    verticalLineTo(68f)
                    arcToRelative(36f, 36f, 0f, false, true, 36f, -36f)
                    horizontalLineToRelative(152f)
                    arcToRelative(36f, 36f, 0f, false, true, 36f, 36f)
                    verticalLineToRelative(348f)
                    horizontalLineToRelative(96f)
                    verticalLineTo(260f)
                    arcToRelative(36f, 36f, 0f, false, true, 36f, -36f)
                    horizontalLineToRelative(140f)
                    arcToRelative(16f, 16f, 0f, false, true, 16f, 16f)
                    verticalLineToRelative(32f)
                    arcToRelative(16f, 16f, 0f, false, true, -16f, 16f)
                    horizontalLineTo(512f)
                    verticalLineToRelative(156f)
                    arcToRelative(36f, 36f, 0f, false, true, -36f, 36f)
                    close()
                }
            }.build()

            return _FontAwesomeWaveSquare!!
        }

    private var _FontAwesomeWaveSquare: ImageVector? = null

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
                    moveTo(19f, 4.5f)
                    curveTo(18.1716f, 4.5f, 17.5f, 5.17157f, 17.5f, 6f)
                    curveTo(17.5f, 6.82843f, 18.1716f, 7.5f, 19f, 7.5f)
                    curveTo(19.8284f, 7.5f, 20.5f, 6.82843f, 20.5f, 6f)
                    curveTo(20.5f, 5.17157f, 19.8284f, 4.5f, 19f, 4.5f)
                    close()
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
                    moveTo(14f, 12.5f)
                    curveTo(13.1716f, 12.5f, 12.5f, 13.1716f, 12.5f, 14f)
                    curveTo(12.5f, 14.8284f, 13.1716f, 15.5f, 14f, 15.5f)
                    curveTo(14.8284f, 15.5f, 15.5f, 14.8284f, 15.5f, 14f)
                    curveTo(15.5f, 13.1716f, 14.8284f, 12.5f, 14f, 12.5f)
                    close()
                    moveTo(5f, 16.5f)
                    curveTo(4.17157f, 16.5f, 3.5f, 17.1716f, 3.5f, 18f)
                    curveTo(3.5f, 18.8284f, 4.17157f, 19.5f, 5f, 19.5f)
                    curveTo(5.82843f, 19.5f, 6.5f, 18.8284f, 6.5f, 18f)
                    curveTo(6.5f, 17.1716f, 5.82843f, 16.5f, 5f, 16.5f)
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

    val FluentuiSystemIconsPause: ImageVector
        get() {
            if (_FluentuiSystemIconsPause != null) return _FluentuiSystemIconsPause!!

            _FluentuiSystemIconsPause = ImageVector.Builder(
                name = "pause",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(5.74609f, 3f)
                    curveTo(4.7796f, 3f, 3.99609f, 3.7835f, 3.99609f, 4.75f)
                    verticalLineTo(19.25f)
                    curveTo(3.99609f, 20.2165f, 4.7796f, 21f, 5.74609f, 21f)
                    horizontalLineTo(9.24609f)
                    curveTo(10.2126f, 21f, 10.9961f, 20.2165f, 10.9961f, 19.25f)
                    verticalLineTo(4.75f)
                    curveTo(10.9961f, 3.7835f, 10.2126f, 3f, 9.24609f, 3f)
                    horizontalLineTo(5.74609f)
                    close()
                    moveTo(14.7461f, 3f)
                    curveTo(13.7796f, 3f, 12.9961f, 3.7835f, 12.9961f, 4.75f)
                    verticalLineTo(19.25f)
                    curveTo(12.9961f, 20.2165f, 13.7796f, 21f, 14.7461f, 21f)
                    horizontalLineTo(18.2461f)
                    curveTo(19.2126f, 21f, 19.9961f, 20.2165f, 19.9961f, 19.25f)
                    verticalLineTo(4.75f)
                    curveTo(19.9961f, 3.7835f, 19.2126f, 3f, 18.2461f, 3f)
                    horizontalLineTo(14.7461f)
                    close()
                }
            }.build()

            return _FluentuiSystemIconsPause!!
        }

    private var _FluentuiSystemIconsPause: ImageVector? = null

    val VscodeCodiconsTriangleRight: ImageVector
        get() {
            if (_VscodeCodiconsTriangleRight != null) return _VscodeCodiconsTriangleRight!!

            _VscodeCodiconsTriangleRight = ImageVector.Builder(
                name = "triangle-right",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
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

    // _____________________ here _____________________ //

    private fun ImageVector.Builder.addCircle(cx: Float, cy: Float, r: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(cx, cy - r)
            curveTo(cx + r * K, cy - r, cx + r, cy - r * K, cx + r, cy)
            curveTo(cx + r, cy + r * K, cx + r * K, cy + r, cx, cy + r)
            curveTo(cx - r * K, cy + r, cx - r, cy + r * K, cx - r, cy)
            curveTo(cx - r, cy - r * K, cx - r * K, cy - r, cx, cy - r)
            close()
        }
    }

    private fun ImageVector.Builder.addCircleOutline(cx: Float, cy: Float, r: Float, color: Color) {
        val ir = r - 0.75f
        path(fill = SolidColor(color)) {
            // Outer
            moveTo(cx, cy - r)
            curveTo(cx + r * K, cy - r, cx + r, cy - r * K, cx + r, cy)
            curveTo(cx + r, cy + r * K, cx + r * K, cy + r, cx, cy + r)
            curveTo(cx - r * K, cy + r, cx - r, cy + r * K, cx - r, cy)
            curveTo(cx - r, cy - r * K, cx - r * K, cy - r, cx, cy - r)
            close()
            // Inner (hole)
            moveTo(cx, cy - ir)
            curveTo(cx - ir * K, cy - ir, cx - ir, cy - ir * K, cx - ir, cy)
            curveTo(cx - ir, cy + ir * K, cx - ir * K, cy + ir, cx, cy + ir)
            curveTo(cx + ir * K, cy + ir, cx + ir, cy + ir * K, cx + ir, cy)
            curveTo(cx + ir, cy - ir * K, cx + ir * K, cy - ir, cx, cy - ir)
            close()
        }
    }

    val AppsColor: ImageVector by lazy {
        ImageVector.Builder("AppsColor", 24.dp, 24.dp, 24f, 24f).apply {
            addCircle(6.5f, 6.5f, 2.5f, Color(0xFF0078D4))
            addCircle(12.0f, 6.5f, 2.5f, Color(0xFF0078D4))
            addCircle(17.5f, 6.5f, 2.5f, Color(0xFF0078D4))
            addCircle(6.5f, 12.0f, 2.5f, Color(0xFF00BCF2))
            addCircle(12.0f, 12.0f, 2.5f, Color(0xFF00BCF2))
            addCircle(17.5f, 12.0f, 2.5f, Color(0xFF00BCF2))
            addCircle(6.5f, 17.5f, 2.5f, Color(0xFF5C2D91))
            addCircle(12.0f, 17.5f, 2.5f, Color(0xFF5C2D91))
            addCircle(17.5f, 17.5f, 2.5f, Color(0xFF5C2D91))
        }.build()
    }

    val AppsRegular: ImageVector by lazy {
        ImageVector.Builder("AppsRegular", 24.dp, 24.dp, 24f, 24f).apply {
            val c = Color.Black
            addCircleOutline(6.5f, 6.5f, 2.5f, c)
            addCircleOutline(12.0f, 6.5f, 2.5f, c)
            addCircleOutline(17.5f, 6.5f, 2.5f, c)
            addCircleOutline(6.5f, 12.0f, 2.5f, c)
            addCircleOutline(12.0f, 12.0f, 2.5f, c)
            addCircleOutline(17.5f, 12.0f, 2.5f, c)
            addCircleOutline(6.5f, 17.5f, 2.5f, c)
            addCircleOutline(12.0f, 17.5f, 2.5f, c)
            addCircleOutline(17.5f, 17.5f, 2.5f, c)
        }.build()
    }

    val AppsFilled: ImageVector by lazy {
        ImageVector.Builder("AppsFilled", 24.dp, 24.dp, 24f, 24f).apply {
            val c = Color.Black
            addCircle(6.5f, 6.5f, 2.5f, c)
            addCircle(12.0f, 6.5f, 2.5f, c)
            addCircle(17.5f, 6.5f, 2.5f, c)
            addCircle(6.5f, 12.0f, 2.5f, c)
            addCircle(12.0f, 12.0f, 2.5f, c)
            addCircle(17.5f, 12.0f, 2.5f, c)
            addCircle(6.5f, 17.5f, 2.5f, c)
            addCircle(12.0f, 17.5f, 2.5f, c)
            addCircle(17.5f, 17.5f, 2.5f, c)
        }.build()
    }

    val MicRegular: ImageVector by lazy {
        ImageVector.Builder("MicRegular", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(9.79f, 2f, 8f, 3.79f, 8f, 6f)
                verticalLineTo(11f)
                curveTo(8f, 13.21f, 9.79f, 15f, 12f, 15f)
                curveTo(14.21f, 15f, 16f, 13.21f, 16f, 11f)
                verticalLineTo(6f)
                curveTo(16f, 3.79f, 14.21f, 2f, 12f, 2f)
                close()
                moveTo(14.5f, 6f)
                verticalLineTo(11f)
                curveTo(14.5f, 12.38f, 13.38f, 13.5f, 12f, 13.5f)
                curveTo(10.62f, 13.5f, 9.5f, 12.38f, 9.5f, 11f)
                verticalLineTo(6f)
                curveTo(9.5f, 4.62f, 10.62f, 3.5f, 12f, 3.5f)
                curveTo(13.38f, 3.5f, 14.5f, 4.62f, 14.5f, 6f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 11f)
                curveTo(19f, 11.41f, 18.66f, 11.75f, 18.25f, 11.75f)
                curveTo(17.84f, 11.75f, 17.5f, 11.41f, 17.5f, 11f)
                curveTo(17.5f, 14.04f, 15.04f, 16.5f, 12f, 16.5f)
                curveTo(8.96f, 16.5f, 6.5f, 14.04f, 6.5f, 11f)
                curveTo(6.5f, 10.59f, 6.16f, 10.25f, 5.75f, 10.25f)
                curveTo(5.34f, 10.25f, 5f, 10.59f, 5f, 11f)
                curveTo(5f, 14.51f, 7.64f, 17.41f, 11f, 17.93f)
                verticalLineTo(21f)
                curveTo(11f, 21.41f, 11.34f, 21.75f, 11.75f, 21.75f)
                curveTo(12.16f, 21.75f, 12.5f, 21.41f, 12.5f, 21f)
                verticalLineTo(17.93f)
                curveTo(15.86f, 17.41f, 18.5f, 14.51f, 18.5f, 11f)
            }
        }.build()
    }

    val CubeRegular: ImageVector by lazy {
        ImageVector.Builder("CubeRegular", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.57f, 2.14f)
                curveTo(11.83f, 2.01f, 12.17f, 2.01f, 12.43f, 2.14f)
                lineTo(19.86f, 5.92f)
                curveTo(20.19f, 6.09f, 20.4f, 6.43f, 20.4f, 6.81f)
                verticalLineTo(15.19f)
                curveTo(20.4f, 15.57f, 20.19f, 15.91f, 19.86f, 16.08f)
                lineTo(12.43f, 19.86f)
                curveTo(12.17f, 19.99f, 11.83f, 19.99f, 11.57f, 19.86f)
                lineTo(4.14f, 16.08f)
                curveTo(3.81f, 15.91f, 3.6f, 15.57f, 3.6f, 15.19f)
                verticalLineTo(6.81f)
                curveTo(3.6f, 6.43f, 3.81f, 6.09f, 4.14f, 5.92f)
                lineTo(11.57f, 2.14f)
                close()
                moveTo(12f, 3.33f)
                lineTo(5.66f, 6.55f)
                lineTo(12f, 9.77f)
                lineTo(18.34f, 6.55f)
                lineTo(12f, 3.33f)
                close()
                moveTo(4.99f, 8.04f)
                verticalLineTo(15.7f)
                lineTo(11.25f, 18.88f)
                verticalLineTo(11.22f)
                lineTo(4.99f, 8.04f)
                close()
                moveTo(12.75f, 18.88f)
                lineTo(19.01f, 15.7f)
                verticalLineTo(8.04f)
                lineTo(12.75f, 11.22f)
                verticalLineTo(18.88f)
                close()
            }
        }.build()
    }

    val BranchRegular: ImageVector by lazy {
        ImageVector.Builder("BranchRegular", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 8.965f)
                curveTo(7.933f, 8.965f, 9.5f, 7.398f, 9.5f, 5.465f)
                curveTo(9.5f, 3.532f, 7.933f, 1.965f, 6f, 1.965f)
                curveTo(4.067f, 1.965f, 2.5f, 3.532f, 2.5f, 5.465f)
                curveTo(2.5f, 7.398f, 4.067f, 8.965f, 6f, 8.965f)
                close()
                moveTo(6f, 15.035f)
                curveTo(4.067f, 15.035f, 2.5f, 16.602f, 2.5f, 18.535f)
                curveTo(2.5f, 20.468f, 4.067f, 22.035f, 6f, 22.035f)
                curveTo(7.933f, 22.035f, 9.5f, 20.468f, 9.5f, 18.535f)
                curveTo(9.5f, 16.602f, 7.933f, 15.035f, 6f, 15.035f)
                close()
                moveTo(18f, 15.035f)
                curveTo(16.067f, 15.035f, 14.5f, 16.602f, 14.5f, 18.535f)
                curveTo(14.5f, 20.468f, 16.067f, 22.035f, 18f, 22.035f)
                curveTo(19.933f, 22.035f, 21.5f, 20.468f, 21.5f, 18.535f)
                curveTo(21.5f, 16.602f, 19.933f, 15.035f, 18f, 15.035f)
                close()
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(6f, 8.965f)
                lineTo(6f, 15.035f)
                moveTo(6f, 12f)
                curveTo(12f, 12f, 12f, 15f, 18f, 15.035f)
            }
        }.build()
    }

    val SettingsRegular: ImageVector by lazy {
        ImageVector.Builder("SettingsRegular", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                curveTo(13.1f, 1f, 14f, 1.9f, 14f, 3f)
                verticalLineTo(3.09f)
                curveTo(14f, 3.57f, 14.3f, 3.99f, 14.75f, 4.16f)
                lineTo(14.83f, 4.19f)
                curveTo(15.28f, 4.36f, 15.79f, 4.29f, 16.17f, 3.99f)
                lineTo(16.23f, 3.94f)
                curveTo(17.06f, 3.29f, 18.25f, 3.39f, 18.96f, 4.16f)
                lineTo(19.84f, 5.04f)
                curveTo(20.61f, 5.75f, 20.71f, 6.94f, 20.06f, 7.77f)
                lineTo(20.02f, 7.83f)
                curveTo(19.72f, 8.21f, 19.65f, 8.72f, 19.82f, 9.17f)
                lineTo(19.84f, 9.25f)
                curveTo(20.01f, 9.7f, 20.43f, 10f, 20.91f, 10f)
                horizontalLineTo(21f)
                curveTo(22.1f, 10f, 23f, 10.9f, 23f, 12f)
                verticalLineTo(13f)
                curveTo(23f, 14.1f, 22.1f, 15f, 21f, 15f)
                horizontalLineTo(20.91f)
                curveTo(20.43f, 15f, 20.01f, 15.3f, 19.84f, 15.75f)
                lineTo(19.82f, 15.83f)
                curveTo(19.65f, 16.28f, 19.72f, 16.79f, 20.02f, 17.17f)
                lineTo(20.07f, 17.23f)
                curveTo(20.72f, 18.06f, 20.62f, 19.25f, 19.85f, 19.96f)
                lineTo(18.97f, 20.84f)
                curveTo(18.26f, 21.61f, 17.07f, 21.71f, 16.24f, 21.06f)
                lineTo(16.18f, 21.02f)
                curveTo(15.8f, 20.72f, 15.29f, 20.65f, 14.84f, 20.82f)
                lineTo(14.76f, 20.84f)
                curveTo(14.31f, 21.01f, 14f, 21.43f, 14f, 21.91f)
                verticalLineTo(22f)
                curveTo(14f, 23.1f, 13.1f, 24f, 12f, 24f)
                horizontalLineTo(11f)
                curveTo(9.9f, 24f, 9f, 23.1f, 9f, 22f)
                verticalLineTo(21.91f)
                curveTo(9f, 21.43f, 8.7f, 21.01f, 8.25f, 20.84f)
                lineTo(8.17f, 20.81f)
                curveTo(7.72f, 20.64f, 7.21f, 20.71f, 6.83f, 21.01f)
                lineTo(6.77f, 21.06f)
                curveTo(5.94f, 21.71f, 4.75f, 21.61f, 4.04f, 20.84f)
                lineTo(3.16f, 19.96f)
                curveTo(2.39f, 19.25f, 2.29f, 18.06f, 2.94f, 17.23f)
                lineTo(2.98f, 17.17f)
                curveTo(3.28f, 16.79f, 3.35f, 16.28f, 3.18f, 15.83f)
                lineTo(3.16f, 15.75f)
                curveTo(2.99f, 15.3f, 2.57f, 15f, 2.09f, 15f)
                horizontalLineTo(2f)
                curveTo(0.9f, 15f, 0f, 14.1f, 0f, 13f)
                verticalLineTo(12f)
                curveTo(0f, 10.9f, 0.9f, 10f, 2f, 10f)
                horizontalLineTo(2.09f)
                curveTo(2.57f, 10f, 2.99f, 9.7f, 3.16f, 9.25f)
                lineTo(3.18f, 9.17f)
                curveTo(3.35f, 8.72f, 3.28f, 8.21f, 2.98f, 7.83f)
                lineTo(2.93f, 7.77f)
                curveTo(2.28f, 6.94f, 2.38f, 5.75f, 3.15f, 5.04f)
                lineTo(4.03f, 4.16f)
                curveTo(4.74f, 3.39f, 5.93f, 3.29f, 6.76f, 3.94f)
                lineTo(6.82f, 3.98f)
                curveTo(7.2f, 4.28f, 7.71f, 4.35f, 8.16f, 4.18f)
                lineTo(8.24f, 4.16f)
                curveTo(8.69f, 3.99f, 9f, 3.57f, 9f, 3.09f)
                verticalLineTo(3f)
                curveTo(9f, 1.9f, 9.9f, 1f, 11f, 1f)
                horizontalLineTo(12f)
                close()
                moveTo(12f, 8f)
                curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f)
                curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
                curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
                curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
                close()
                moveTo(12f, 9.5f)
                curveTo(13.38f, 9.5f, 14.5f, 10.62f, 14.5f, 12f)
                curveTo(14.5f, 13.38f, 13.38f, 14.5f, 12f, 14.5f)
                curveTo(10.62f, 14.5f, 9.5f, 13.38f, 9.5f, 12f)
                curveTo(9.5f, 10.62f, 10.62f, 9.5f, 12f, 9.5f)
                close()
            }
        }.build()
    }
}
