# Implementation Plan - Reset Harmonic Overrides on New Input

The goal is to ensure that manual tweaks made in the "Signal Decomposition" menu do not persist when the user provides a new input (e.g., drawing a new line or importing a new SVG). Currently, these overrides (frequency, amplitude, phase, paused/removed status) remain in memory, causing subsequent drawings to be distorted by old settings.

## User Review Required

> [!IMPORTANT]
> When you draw a new line, all previous manual edits (like frequency changes or removed harmonics) will be automatically reset to their defaults. This ensures you are always looking at a clean representation of your new drawing.

## Proposed Changes

### Fourier State Management

#### [MODIFY] [FourierState.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierState.kt)
- Add a helper function `clearOverrides()` that clears:
    - `pausedHarmonics`
    - `removedHarmonics`
    - `harmonicFrequencies`
    - `harmonicAmplitudes`
    - `harmonicPhases`
    - Increments `harmonicVersion`.
- Call `clearOverrides()` at the start of:
    - `calculateDFT()`
    - `calculateDFT2D()`
    - `calculateSVGDFT()`
- Also call it in `resetSimulation()`? (Maybe not, user might want to reset the simulation path without losing edits).
- Call it when `formulaString` changes to ensure new formulas start fresh.

## Verification Plan

### Automated Tests
- Build project: `./gradlew :app:assembleDebug`

### Manual Verification
1. Open **Fourier series -> Draw -> 1D**.
2. Draw a simple line.
3. In the edit menu, change the frequency of the first harmonic and remove the second harmonic.
4. **Verify**: Simulation updates and reflects these changes.
5. Draw a *new* shape on the canvas.
6. **Verify**: The simulation immediately reflects the new shape perfectly.
7. **Verify**: Open the edit menu and check that the frequency is back to its default (0 for DC or 1 for first harmonic) and the second harmonic is no longer removed.
8. Repeat for **2D Drawing** and **Formula** mode.
