# Tasks - Split Large Files

## Theme Refactoring
- [x] Split `Theme.kt` into smaller files:
    - [x] `Colors.kt`
    - [x] `Typography.kt`
    - [x] `Dimensions.kt`
    - [x] `Animations.kt`
    - [x] `CommonUI.kt`
    - [x] `AppTheme.kt`
- [x] Update imports for Theme components
- [x] Delete `Theme.kt`

## Fourier Components Refactoring
- [x] Split `FourierComponents.kt` into:
    - [x] `FourierGraph.kt`
    - [x] `FourierDialogs.kt`
    - [x] `FourierHarmonics.kt`
    - [x] `FourierButtons.kt`
- [x] Extract sidebar from `FourierVisualizer.kt` into `FourierSidebar.kt`
- [x] Move `SignalSettingsCard` from `FourierSettingsComponents.kt` to `FourierCards.kt`
- [x] Update imports for Fourier components
- [x] Delete `FourierComponents.kt`

## Verification
- [ ] Run `./gradlew :app:compileDebugKotlin`
