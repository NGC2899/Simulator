# Implementation Plan - Fix Reset Behavior for Custom Fourier Signals

The "Reset" functionality in the Fourier series "Custom -> Signal" mode is currently ineffective because it reloads signals from persistent storage, which has already been updated with the latest edits. This makes the edits become the new "default" immediately.

## Proposed Changes

### [Fourier UI]

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierSeries.kt)
- Update `onResetHarmonics` (global reset for the harmonics list) for the `PURE_SIGNAL` case.
- Instead of clearing the list and reloading from `prefs`, iterate through the current `customFunctionSignals` and reset each `SignalInstance` to its `initial` values (`freq = initialFreq`, etc.).
- This ensures that tweaks made via sliders in the "Edit Menu" (popup) can be reverted to the values they had when the signals were first added or loaded.
- Apply this fix to both `HarmonicComponents` and `ComplexHarmonicComponents` instances.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:compileDebugKotlin`

### Manual Verification
1. Navigate to **Fourier Series -> Custom -> Signal**.
2. Add a few signal components.
3. Use the "Edit Menu" (three dots icon) on a component to change its frequency/amplitude via sliders.
4. Click the "Reset" button in the "Signal Decomposition" header.
5. Verify that the sliders and the simulation revert to the values entered in the text fields (or the defaults when added).
6. Verify that "Reset to Default" for an individual signal in the popup menu also works.
