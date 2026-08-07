# Fix Argument Type Mismatch Errors

Multiple compilation errors have occurred because the `SignalInstance` class now expects a `Color` object for its `colorArgb` property, while several parts of the codebase were still passing an `Int` or wrapping the `Color` incorrectly.

## Proposed Changes

### [Persistence]

#### [MODIFY] [Persistence.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Persistence.kt)
- Update `saveFourierSignals` to explicitly convert the `Color` property to an ARGB `Int` when serializing to `SharedPreferences`.
- Update `loadFourierSignals` to wrap the ARGB `Int` (from both the default parameter and the stored data) into a `Color` object when instantiating `SignalInstance`.

### [Fourier UI]

#### [MODIFY] [FourierSettingsComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierSettingsComponents.kt)
- In the "Add Component" logic, pass the `Color` object directly to the `SignalInstance` constructor instead of calling `.toArgb()`.
- In `SignalSettingsCard`, remove the redundant `Color()` constructor call since `signal.colorArgb` is already a `Color`.

#### [MODIFY] [FourierVisualizer.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierVisualizer.kt)
- In the visualization loop, remove the redundant `Color()` constructor call when retrieving the term color from `SignalInstance`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure all compilation errors are resolved.
- Run existing unit tests: `./gradlew :app:test`

### Manual Verification
- Deploy the app and verify that Fourier signal colors are correctly saved and loaded.
- Verify that adding a new signal component works and assigns a valid color.
