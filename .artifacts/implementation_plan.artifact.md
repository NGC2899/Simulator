# Implementation Plan - Export Fourier Series

The user wants an "Export" button in the "Signal Decomposition" and "Phasor Decomposition" boxes. This button will open a dialog showing the Fourier series in both "Normal" (Trigonometric/Parametric) and "Complex" (Exponential) forms, with "Copy" buttons for each.

## Proposed Changes

### Fourier Module

#### [MODIFY] [FourierComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierComponents.kt)
- **Export Button**: Add an "Export" `TextButton` next to the "Reset" button in both `HarmonicComponents` and `ComplexHarmonicComponents`.
- **Export Dialog**: Implement a `FourierExportDialog` composable that:
    - Takes the current harmonics data (frequencies, amplitudes, phases).
    - Formats them into strings for "Normal" and "Complex" series.
    - Displays them in read-only text fields with "Copy" buttons.
    - Uses `LocalClipboardManager` for copying.

### Logic Improvements

#### [NEW] [FourierExportLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierExportLogic.kt)
- Create a helper object to generate the series strings.
- **Normal Form (1D)**: $f(t) = \sum A_n \cos(2\pi f_n t + \phi_n)$
- **Normal Form (2D/Complex)**:
    - $x(t) = \sum A_n \cos(2\pi f_n t + \phi_n)$
    - $y(t) = \sum A_n \sin(2\pi f_n t + \phi_n)$
- **Complex Form**: $f(t) = \sum A_n e^{i (2\pi f_n t + \phi_n)}$
- Filter out terms with near-zero amplitude.
- Round values for readability (e.g., 2 decimal places).

## Verification Plan

### Manual Verification
1.  Navigate to the Fourier simulation.
2.  Choose a wave type (e.g., Square).
3.  Click the **Export** button in "Signal Decomposition".
4.  Verify the dialog opens and shows the correct formula components (e.g., frequencies 1, 3, 5 for Square).
5.  Click **Copy** for one of the fields and paste it somewhere to verify the content.
6.  Modify a harmonic using the edit menu (e.g., change amplitude).
7.  Re-open **Export** and verify the string reflects the change.
8.  Repeat for "Complex" display mode and verify the 2D parametric/complex output.
