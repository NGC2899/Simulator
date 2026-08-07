# Implementation Plan - Split Large Files

This plan outlines the refactoring of `FourierComponents.kt` and `Theme.kt` into smaller, logically grouped files to improve maintainability and readability.

## User Review Required

> [!IMPORTANT]
> Some components currently in `Theme.kt` (like `DisplayModeButton` and `SidebarActionButton`) are used across both Fourier and Pendulum modules. While the user suggested `FourierButtons.kt`, I propose creating a `SharedComponents.kt` or similar for these if they remain shared. However, for now, I will stick as close to the suggested names as possible.

## Proposed Changes

### [Theme & Shared UI]

Move contents of `app/Theme.kt` into the following new files in `com.example.matharium.ui.theme` (or same package as before):

#### [NEW] [Colors.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Colors.kt)
- `DarkColors`
- `LightColors`
- `AppColors`
- `LocalAppColors`

#### [NEW] [Typography.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Typography.kt)
- Typography related constants from `AppDesign` (e.g., `textCaption`, `textHeadline`, etc.)

#### [NEW] [Dimensions.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Dimensions.kt)
- Spacing, radius, icon sizes, and other Dp constants from `AppDesign`.

#### [NEW] [Animations.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Animations.kt)
- `AnimatedBlobBackground`
- `BlobAnimParams`
- Animation duration constants from `AppDesign`.

#### [NEW] [AppTheme.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/AppTheme.kt)
- `AppTheme` composable.
- `GlassCard` (Common UI component).

#### [NEW] [CommonUI.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/CommonUI.kt) (or similar)
- `LabeledSlider`
- `ToggleRow`
- `rememberAppVibrator`
- `Modifier.hapticClickable`

#### [DELETE] [Theme.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Theme.kt)

---

### [Fourier Components]

Move contents of `fourier/ui/FourierComponents.kt` into the following:

#### [NEW] [FourierGraph.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierGraph.kt)
- `FrequencyDomainGraph`

#### [NEW] [FourierDialogs.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierDialogs.kt)
- `FourierExportDialog`
- `ExportField`

#### [NEW] [FourierHarmonics.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierHarmonics.kt)
- `HarmonicComponents`
- `HarmonicItemRow`
- `ComplexHarmonicComponents`
- `ComplexHarmonicItemRow`

#### [MODIFY] [FourierControls.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierControls.kt)
- Keep `FourierActionControls`.
- Move `DisplayModeButton` and `SidebarActionButton` here if they are only for Fourier, or keep in `CommonUI.kt`. (I will move them to a new `FourierButtons.kt` as requested, but might need to adjust imports in Pendulum).

#### [NEW] [FourierButtons.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierButtons.kt)
- `DisplayModeButton`
- `SidebarActionButton`

#### [NEW] [FourierCards.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierCards.kt)
- `SignalSettingsCard` (from `FourierSettingsComponents.kt`)

#### [DELETE] [FourierComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/ui/FourierComponents.kt)

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure all imports are correctly updated and the project builds.

### Manual Verification
- Verify that the app still looks and behaves the same, especially the Fourier series and Theme switching.
