# Implementation Plan - Add Starting Angle (Phase) to Custom Signals

This plan adds a "Starting Angle" (phase) field to each custom signal component in the Fourier Series simulator. This allows users to distinguish between sine waves, cosine waves, and anything in between.

## Proposed Changes

### [Component: Data Model]

#### [MODIFY] [FourierModels.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierModels.kt)
- Add `initialPhase: String` to `SignalInstance` constructor.
- Add `phase` state property.
- Add `cachedPhase` for the simulation loop.
- Update `updateCache()` to parse the phase string (converting degrees to radians).

### [Component: Persistence]

#### [MODIFY] [Persistence.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Persistence.kt)
- Update `saveFourierSignals` and `loadFourierSignals` to include the phase value in the serialized string.

### [Component: UI - Settings]

#### [MODIFY] [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Add a new `SignalField` for "Phase" in the `SignalSettingsCard`.
- Unit: Degrees (°).

### [Component: Simulation & Visualization]

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Update the `PURE_SIGNAL` logic in the simulation loop and spectrum calculation to include `signal.cachedPhase`.

#### [MODIFY] [FourierVisualizer.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierVisualizer.kt)
- Update the `PURE_SIGNAL` drawing logic in `FourierVisualizerBox` to include the phase.

#### [MODIFY] [FourierComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierComponents.kt)
- Update the harmonic preview logic in `HarmonicComponents` and `ComplexHarmonicComponents` to account for the custom signal's phase.

## Verification Plan

### Manual Verification
- Deploy the app and go to **Fourier Series -> Custom**.
- Add a component.
- Change the **Phase** field (e.g., set it to 90°).
- Verify that the rotating circle for that component starts at the top (cosine-like) instead of the right (sine-like).
- Verify that the generated graph shifts accordingly.
- Restart the app and verify the phase setting is persisted.
