# Walkthrough - Fix Locale-Sensitive Numeric Formatting

I have fixed the issue where sliders in the "Custom Mode -> Signal" edit menu would jump to 0 immediately upon interaction. This was caused by locale-sensitive decimal separators (commas) breaking the internal numeric parsing.

## Changes Made

### Unified Numeric Formatting

#### [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- **Locale-Aware Formatting**: Updated all numeric formatting logic for `SignalInstance` to explicitly use `java.util.Locale.US`.
- **Consistency**: This ensures that regardless of the device locale (e.g., German, Persian), decimal values are always stored and parsed using the dot (`.`) separator, which is what `toFloatOrNull()` expects.
- **Affected Parameters**:
    - Frequency (Hz)
    - Amplitude
    - Phase (Degrees)

### Verification

#### Unit Test Logic
- Added a new test case `testLocaleSensitiveParsing` to [FourierLogicTest.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/test/java/com/example/matharium/fourier/FourierLogicTest.kt).
- The test demonstrates that:
    1.  `"1,50".toFloatOrNull()` returns `null` (causing the jump to 0).
    2.  Formatting with `Locale.US` produces `"1.50"`, which parses correctly back to `1.5f`.

#### Manual Logic Review
- Verified that all `String.format` calls in the settings UI now either use `Locale.US` or are only used for display (where locale-sensitive formatting is actually desired).
- Confirmed that the `onValueChange` listeners in `FourierSeries.kt` correctly update the `SignalInstance` cache after formatting the string correctly.

> [!NOTE]
> This fix prevents the "jump to 0" bug for users with non-US regional settings while maintaining correct simulation behavior. All internal mathematical calculations already use `Float` values, so this change only affects how these values are serialized to strings in the UI state.
