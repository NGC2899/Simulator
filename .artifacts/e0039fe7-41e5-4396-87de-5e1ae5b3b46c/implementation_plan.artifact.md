# Implementation Plan - Performance Optimization and Bug Fixing

This plan focuses on optimizing the Fourier simulation engine and UI, improving mathematical expression evaluation, and ensuring the app is production-ready.

## User Review Required

> [!IMPORTANT]
> Enabling R8 (minifyEnabled) may impact debugging. I will enable it only for the `release` build type as per standard practice.

## Proposed Changes

### Core Logic & Mathematics

#### [MODIFY] [FourierExpression.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierExpression.kt)
- Refactor `FourierExpressionEvaluator` to separate **parsing** from **evaluation**.
- Implement a simple AST (Abstract Syntax Tree) to allow compiled expressions to be evaluated multiple times without re-parsing.

#### [MODIFY] [FourierLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierLogic.kt)
- Compile the `tokenRegex` in `extractPointsFromSVG` once instead of every time it matches a path.
- Add better error handling to `getIdealValue` for edge cases.

### State Management & Performance

#### [MODIFY] [FourierState.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierState.kt)
- Introduce a unified `activeHarmonics` state that stores precomputed frequency, amplitude, and phase for the current wave type.
- Update this list only when the wave type, coefficients, or formula changes.
- Use `derivedStateOf` for UI-dependent properties like `isSimulationEnabled`.
- Integrate the new compiled expression logic for `FORMULA` mode.

#### [MODIFY] [FourierVisualizer.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierVisualizer.kt)
- Simplify the drawing loop in `FourierVisualizerBox`. Instead of a massive `when` block inside the loop, it will iterate over `state.activeHarmonics`.
- This will significantly reduce the CPU load during drawing, especially with 250+ terms.

### Build & Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/build.gradle.kts)
- Set `isMinifyEnabled = true` in the `release` build type.
- Set `isShrinkResources = true` to remove unused resources.

## Verification Plan

### Automated Tests
- Run existing `FourierLogicTest`.
- Add a new test for the AST-based `FourierExpressionEvaluator` to ensure mathematical correctness.

### Manual Verification
- Verify that all wave types (Sine, Square, Triangle, Sawtooth, Formula, Draw, SVG) still work correctly.
- Compare UI responsiveness (FPS) before and after optimization, especially in `FORMULA` mode with complex expressions.
- Verify that the app still builds and runs in both `debug` and `release` modes.
