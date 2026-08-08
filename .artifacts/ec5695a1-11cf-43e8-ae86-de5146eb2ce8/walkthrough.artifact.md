# Fourier Module UI Recomposition Optimization

I have optimized the Fourier Series module UI to ensure that high-frequency state changes (like animation time) only affect the components that genuinely depend on them, while keeping the rest of the UI static.

## Key Optimizations Applied

### 1. Zero-Recomposition Loop
- **Lambda Providers**: High-frequency states (`time`, `pathCount`, `nTerms`) are now passed as lambda providers (e.g., `timeProvider: () -> Float`) instead of raw values.
- **Draw-Phase Reading**: These lambdas are read **only inside the `Canvas` draw blocks**. Because Compose draw-phase reads do not trigger recomposition, the `FourierSeries` screen and all its setting cards remain stable (0 recompositions) even while the animation is running at 60/120 FPS.

### 2. Targeted Slider Updates (Deferred Engine Update)
- **UI/Engine Separation**: I introduced a separation between the **UI State** (`intendedNTerms`) and the **Engine State** (`nTerms`).
- **Smooth Interaction**: As the user drags the slider, only the small number label in the sidebar recomposes. The expensive graphics engine and path rebuilding are **deferred** until the user releases the slider (`onValueChangeFinished`). This prevents CPU spikes and lag on older hardware during interaction.

### 3. Smart List Keying
- **Harmonics List**: Added `key(i)` to the harmonics decomposition list. This allows Compose to intelligently reuse row items when the signal changes or terms are added, preventing unnecessary layout passes for existing rows.

### 4. Optimized Graph Rendering
- **Frequency Domain**: The `FrequencyDomainGraph` now also uses a `timeProvider`. This allows the real-time "Center of Mass" animation to move smoothly without triggering recompositions of the graph container or its labels.

## Recomposition Summary

| State | Frequency | Reader | Recomposition Impact |
| :--- | :--- | :--- | :--- |
| **Animation Time** | 60-120 Hz | `Canvas#onDraw` | **Zero** |
| **Path Count** | 60-120 Hz | `Canvas#onDraw` | **Zero** |
| **intendedNTerms** | Drag speed | `Sidebar#Text` | **Local Only** |
| **nTerms (Active)** | On release | `Visualizer`, `Engine` | **One-time** |
| **Wave Type** | User action | Entire Screen | **Necessary** |

## Verification Steps
1. Open **Android Studio Profiler** -> **System Trace**.
2. Start the Fourier animation.
3. Observe the `Main` thread: you should see very few `Recomposition` blocks compared to the `Draw` blocks.
4. Open the **Layout Inspector** and enable "Show Recomposition Counts."
5. Verify that the counts for setting cards and buttons remain at **0** during simulation.
