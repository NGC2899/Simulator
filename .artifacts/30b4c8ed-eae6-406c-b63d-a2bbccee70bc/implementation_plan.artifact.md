# Dynamic Error Sensitivity Control

This plan adds a user-controlled slider to adjust the sensitivity of the error gradient in the Fourier simulation.

## Proposed Changes

### [Persistence.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Persistence.kt)
- Add `fourierErrorSensitivity` property to `AppPreferences` for persistence.
- **Default value**: `20.0f`.

### [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Add `errorSensitivity` state synchronized with `prefs`.
- Pass `errorSensitivity` and `onErrorSensitivityChange` to child components.

### [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Add a new "Error Sensitivity" `LabeledSlider` in the settings card.
- **UI Logic**: Only show the slider when "Enable error gradient" is checked.

### [FourierVisualizer.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierVisualizer.kt)
- Update `FourierVisualizerBox` to accept and use the dynamic `errorSensitivity` value for calculating the color gradient.

## Verification Plan
### Manual Verification
1.  Open the **Fourier Series** simulator.
2.  Enable **"Enable error gradient"** in settings.
3.  Verify a new **"Error Sensitivity"** slider appears.
4.  Adjust the slider and verify that:
    - Moving it to the left (lower value) makes the graph **more violet** (stricter accuracy requirement).
    - Moving it to the right (higher value) makes the graph **more blue** (relaxed accuracy requirement).
5.  Verify the setting persists after app restart.
