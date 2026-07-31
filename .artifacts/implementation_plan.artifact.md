# Implementation Plan - Fix Locale-Sensitive Numeric Formatting

The user reported that sliders in the "Custom Mode -> Signal" edit menu jump to 0 immediately upon interaction. This is caused by locale-sensitive formatting of numeric strings. In locales that use a comma as a decimal separator (e.g., German, Persian), `String.format("%.2f", value)` produces strings like `"1,50"`. However, Kotlin's `toFloatOrNull()` always expects a dot (`.`) as the decimal separator, causing it to return `null` (and subsequently `0f` via the elvis operator) for these locale-formatted strings.

## User Review Required

> [!IMPORTANT]
> This fix will force `Locale.US` for all internal numeric string formatting used for state storage and simulation parameters. This ensures that the app behaves consistently across all device locales and that sliders/text fields don't "break" when the device is set to a non-US locale.

## Proposed Changes

### Fourier Module

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Update all `onFrequencyChange`, `onAmplitudeChange`, and `onPhaseChange` lambdas to use `Locale.US` when formatting strings for `SignalInstance`.
    - Change `"%.2f".format(f)` to `java.util.Locale.US.let { "%.2f".format(it, f) }` or similar.

#### [MODIFY] [FourierSettingsComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettingsComponents.kt)
- Ensure any other numeric formatting that might be read back into state also uses `Locale.US`.

#### [MODIFY] [FourierModels.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierModels.kt)
- In `SignalInstance.updateCache()`, the `toFloatOrNull()` calls are correct, but they are receiving the broken strings. Fixing the formatting in `FourierSeries.kt` will resolve this.

### Verification Plan

#### Automated Tests
- I will add a unit test to `FourierLogicTest.kt` (or a new test file) that specifically tests formatting a float with a non-US locale (e.g., German) and verify that it fails to parse with `toFloatOrNull()`, then verify that forcing `Locale.US` fixes it.

#### Manual Verification
- Deploy the app.
- Change the device locale to one that uses commas (e.g., German or Persian) if possible, or simulate the behavior by manually forcing a comma in a test string.
- Navigate to "Custom Mode -> Signal" and open the edit menu for a component.
- Move the sliders and verify they update correctly and don't jump to 0.
