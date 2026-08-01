# Tasks - Optimize 1D Drawing and Fix Immediate Update

- `[x]` Optimize `FourierState.kt`
    - `[x]` Add `drawingVersion` and `drawing2DVersion` state variables
    - `[x]` Refine `calculateDFT`, `calculateDFT2D`, and `updateSpectrum` job management
- `[x]` Update `FourierSeries.kt`
    - `[x]` Replace list-based `LaunchedEffect` keys with version counters
    - `[x]` Adjust debounce timings for smoother UI
- `[x]` Update `FourierSettingsComponents.kt`
    - `[x]` Use version counters in `onDrag` and `onDragEnd`
    - `[x]` Implement "Auto-Play" on drag end
    - `[x]` Optimize drawing `Canvas` to reduce allocations
- `[x]` Verify build and manual simulation
