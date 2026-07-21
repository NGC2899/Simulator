# Symmetry Detector Implementation Plan

Add a symmetry detector to the Fourier Series feature to identify even and odd functions, providing feedback to the user about which coefficients are dominant.

## Proposed Changes

### [FourierLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierLogic.kt)
- Add `SymmetryResult` data class.
- Add `detectSymmetry(samples: List<Float>)` function.

### [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Add `symmetryResult` state.
- Update `calculateDFT()` to trigger symmetry detection.
- Pass `symmetryResult` to `FourierSettingsCard`.

### [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Add `symmetryResult` parameter to `FourierSettingsCard`.
- Display the symmetry message below the formula input field.

## Verification Plan

### Manual Verification
1. Open Fourier Series.
2. Select "Custom" (Formula).
3. Enter `cos(x)` (Even) -> Expect "This function is symmetric and is even therefore only cosine coefficients exist."
4. Enter `sin(x)` (Odd) -> Expect "This function is symmetric and is odd therefore only sine coefficients exist."
5. Enter `cos(x) + 0.1 * sin(x)` -> Expect approximation message like "This function is 97% even so we can safely ignore sine coefficients."
