# Implementation Plan - Optimize 1D Drawing and Fix Immediate Update

The user reports that 1D drawing changes are only visible after a restart/tab switch and that the app becomes glitchy during drawing. This is likely due to excessive recompositions and heavy background calculations triggered by frequent list copying and O(N) comparisons in `LaunchedEffect` keys.

## User Review Required

> [!IMPORTANT]
> To fix the "glitchiness," I will implement a "versioned" update system. Instead of observing the entire 1000-point list for changes, the app will react to a version counter.
> I will also enable "Auto-Play" after drawing, so the simulation starts immediately without requiring manual interaction.

## Proposed Changes

### Fourier State Management

#### [MODIFY] [FourierState.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierState.kt)
- Add `drawingVersion` and `drawing2DVersion` as `mutableIntStateOf(0)`.
- Update `calculateDFT` and `calculateDFT2D` to be cancellable (ensure `dftJob` management is robust).
- Add `spectrumJob` management to `updateSpectrum` to prevent job piling.
- Optimize `updateSpectrum` to skip calculation if not necessary or during active drag.

### Fourier UI Components

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Replace `state.drawingPoints.toList()` with `state.drawingVersion` in all `LaunchedEffect` keys.
- Replace `state.drawingPoints2D.toList()` with `state.drawing2DVersion`.
- Adjust debouncing: short debounce for persistence, longer for heavy spectrum updates.

#### [MODIFY] [FourierSettingsComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettingsComponents.kt)
- Increment `drawingVersion` / `drawing2DVersion` in `onDrag` (with throttling) or `onDragEnd`.
- Trigger `state.running = true` in `onDragEnd` to show results immediately.
- Optimize `Canvas` drawing to reduce object allocation.

## Verification Plan

### Automated Tests
- Build project: `./gradlew :app:assembleDebug`

### Manual Verification
1. Open **Fourier series -> Draw -> 1D**.
2. Draw a shape.
3. **Verify**: The drawing experience is smooth (no lag/glitches).
4. **Verify**: The simulation starts automatically and accurately reflects the drawn shape as soon as the finger/mouse is lifted.
5. **Verify**: Switching between 1D and 2D drawing works and simulation updates correctly for both.
6. **Verify**: The frequency spectrum updates without freezing the UI.
