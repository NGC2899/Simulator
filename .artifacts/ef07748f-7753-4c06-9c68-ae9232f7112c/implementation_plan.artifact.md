# Implementation Plan - Symmetry Detector for Fourier Series

Implement a symmetry detector that analyzes custom formulas and drawings to identify even or odd symmetry, providing feedback on dominant Fourier coefficients.

## User Review Required

> [!NOTE]
> Symmetry detection works by comparing the function values at $x$ and $2\pi - x$.
> It requires the function to be sampled over a full period $[0, 2\pi]$.

## Proposed Changes

### [Fourier Logic]

#### [MODIFY] [FourierLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierLogic.kt)
- Add `SymmetryResult` data class: `val evenPercent: Float`, `val oddPercent: Float`.
- Add `detectSymmetry(samples: List<Float>)` function.
    - Compares $f(i)$ and $f(N-i)$ for $i=1 \dots N/2$.
    - Calculates the degree of match for Even ($f(x) \approx f(-x)$) and Odd ($f(x) \approx -f(-x)$) symmetry.
    - Factors out the DC offset for odd symmetry detection.

### [Fourier UI]

#### [MODIFY] [FourierSeries.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSeries.kt)
- Add `var symmetryResult by remember { mutableStateOf<FourierLogic.SymmetryResult?>(null) }`.
- Update `calculateDFT()` to trigger `FourierLogic.detectSymmetry(samples)` and store the result.
- Pass `symmetryResult` to `FourierSettingsCard`.

#### [MODIFY] [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Add `symmetryResult: FourierLogic.SymmetryResult?` parameter to `FourierSettingsCard`.
- Implement a helper composable `SymmetryMessage(result: SymmetryResult?)` to display the specific strings requested by the user:
    - If $>99.5\%$ Even: "This function is symmetric and is even therefore only cosine coefficients exist."
    - If $>99.5\%$ Odd: "This function is symmetric and is odd therefore only sine coefficients exist."
    - If $>85\%$ Even/Odd: "This function is X% even/odd so we can safely ignore sine/cosine coefficients."
- Show this message below the Formula text field and the Drawing area.

## Verification Plan

### Manual Verification
1.  **Even Function**: Enter `cos(x)`. Verify the "even therefore only cosine" message appears.
2.  **Odd Function**: Enter `sin(x)`. Verify the "odd therefore only sine" message appears.
3.  **Approximate Even**: Enter `cos(x) + 0.05 * sin(x)`. Verify the "95% even so we can safely ignore sine" message (approx).
4.  **No Symmetry**: Enter `exp(x)`. Verify no symmetry message is shown (or it's below threshold).
5.  **Drawing**: Draw a symmetric parabola in "Draw" mode and check if symmetry is detected.
