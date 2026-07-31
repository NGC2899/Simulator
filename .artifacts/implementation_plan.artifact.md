# Implementation Plan - Fix Center of Mass Graph for 2D Inputs

The "Frequency Domain (Center of Mass)" graph currently only supports 1D signals (Sine, Square, etc.). When the user selects SVG or 2D Drawing mode, the spectrum calculation returns an empty list, causing the graph to disappear.

## Proposed Changes

### Fourier Module

#### [MODIFY] [FourierState.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierState.kt)
- **Support 2D in `updateSpectrum`**:
    - Update the `when` block in `updateSpectrum` to handle `WaveType.MY_SIGNAL_2D` and `WaveType.SVG`.
    - Change the internal `samples` collection to use `FourierLogic.Complex` instead of `Float`. This allows us to unify the logic for both 1D and 2D signals.
    - **1D Signals**: Map to `Complex(value, 0.0)`.
    - **2D Signals**: Map to `Complex(x, y)`.
- **Complex Integration**:
    - Update the spectrum integral to use complex multiplication: $z(t) \cdot e^{i \omega t}$.
    - This will correctly calculate the "Center of Mass" for 2D paths. For example, a circle with frequency 1 will show a strong peak at 1Hz in the spectrum.

## Verification Plan

### Manual Verification
1.  Navigate to the Fourier simulation.
2.  Switch to **Draw 2D** mode and draw a circle or a complex shape.
3.  **Verify**: The "Frequency Domain" graph should now appear and show peaks corresponding to the dominant frequencies of the drawing.
4.  Switch to **Import SVG** and load a file.
5.  **Verify**: The graph should populate with the spectrum data for the SVG path.
6.  Switch back to **Square Wave** (1D).
7.  **Verify**: The 1D spectrum continues to function correctly (peaks at odd harmonics 1, 3, 5...).
