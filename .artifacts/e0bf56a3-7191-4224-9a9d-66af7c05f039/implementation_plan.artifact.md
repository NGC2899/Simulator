# Implementation Plan - Add Wave Compression/Stretch Handler

The user wants a separate handler to adjust the "compression" (horizontal scale) of the Fourier graph, as the previous hardcoded adjustment wasn't sufficient for all cases.

## User Review Required

> [!NOTE]
> I will add a new slider called "Wave Stretch" in the Simulator Settings. This will allow the user to dynamically adjust how "stretched" or "compressed" the wave appears in Circular mode.

## Proposed Changes

### [Component: Persistence]

#### [MODIFY] [Persistence.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Persistence.kt)
- Add `fourierWaveStretch` property (Float) to `AppPreferences`. Default value will be `120.0f` (the current hardcoded value).

### [Component: UI - Fourier Series State]

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Declare `waveStretch` state initialized from `prefs.fourierWaveStretch`.
- Add a `LaunchedEffect` to persist `waveStretch` when it changes.
- Pass `waveStretch` to `FourierVisualizerBox` and `FourierSettingsCard`.

### [Component: UI - Fourier Visualizer]

#### [MODIFY] [FourierVisualizer.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierVisualizer.kt)
- Add `waveStretch: Float` parameter to `FourierVisualizerBox`.
- Use `waveStretch.dp.toPx()` instead of the hardcoded `120.dp.toPx()` for `pixelsPerTimeUnit`.

### [Component: UI - Fourier Settings]

#### [MODIFY] [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Add `waveStretch: Float` and `onWaveStretchChange: (Float) -> Unit` parameters to `FourierSettingsCard`.
- Add a new `LabeledSlider` for "Wave Stretch" in the settings column. Range: `30f` to `300f`.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to Fourier Series.
- Open "Simulator Settings".
- Adjust the "Wave Stretch" slider.
- Verify that the wave in Circular mode stretches horizontally as the value increases and compresses as it decreases.
- Verify that the setting is persisted after restarting the app.
