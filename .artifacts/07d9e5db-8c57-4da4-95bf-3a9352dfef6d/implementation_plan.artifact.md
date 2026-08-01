# Implementation Plan - Reset Overrides for All Wave Samples

The goal is to ensure that manual harmonic overrides (frequency, amplitude, phase, etc.) are cleared not just for custom drawings, but also when switching between standard wave samples (Sine, Square, Sawtooth, Triangle). Currently, if a user edits a harmonic in "Square" mode and then switches to "Triangle", the edit persists, resulting in a distorted Triangle wave.

## User Review Required

> [!IMPORTANT]
> Switching between any wave type (e.g., from Sine to Square, or from Draw to Triangle) will now automatically reset all manual edits to the harmonics. This ensures that every sample starts with its mathematically correct "default" state.

## Proposed Changes

### Fourier State Management

#### [MODIFY] [FourierState.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierState.kt)
- Convert `waveType` from a simple `mutableStateOf` to a property with a custom setter (or handle it in the `LaunchedEffect` in `FourierSeries.kt`).
- A cleaner way in the current architecture is to call `clearOverrides()` in the `LaunchedEffect(state.waveType)` block in `FourierSeries.kt`.
- However, since `calculateDFT` already calls `clearOverrides()`, we just need to ensure the standard types do it too.

### UI Reactivity

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Update the `LaunchedEffect(state.waveType)` to call `state.clearOverrides()` for ALL wave types.
- Ensure `state.resetSimulation()` is also called to prevent old trail paths from being drawn with the new wave type.

## Verification Plan

### Automated Tests
- Build project: `./gradlew :app:assembleDebug`

### Manual Verification
1. Open **Fourier series**.
2. Select **Square** wave.
3. In the edit menu, change the frequency of the first harmonic.
4. Switch to **Triangle** wave.
5. **Verify**: The Triangle wave is perfectly formed (not distorted by the previous frequency edit).
6. **Verify**: Open the decomposition menu and check that the first harmonic frequency is back to its default (`1.0`).
7. Switch back to **Square** and verify it is also back to default.
8. Draw something, edit a signal, then switch to **Sawtooth**. Verify Sawtooth is clean.
