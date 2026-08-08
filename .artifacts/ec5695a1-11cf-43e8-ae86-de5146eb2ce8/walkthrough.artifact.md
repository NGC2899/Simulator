# Fourier Synthesis Optimization Walkthrough

We have optimized the Fourier visualizer to distinguish between **Signal Analysis** (heavy DFT) and **Signal Synthesis** (circles summation). This resolves the issue where changing the number of terms appeared to trigger a full re-analysis of the signal.

## Changes Made

### 1. Separated Analysis and Synthesis
- **isAnalyzing**: Only active during initial DFT (Drawing, SVG import, Formula change).
- **isSynthesizing**: Active when rebuilding the synthesis cache (e.g., when sliding `nTerms`).
- **UI Update**: The "Analyzing Signal..." status indicator now only appears when `isAnalyzing` is true, providing clearer feedback to the user.

### 2. Optimized Cache Rebuild
- **Ideal Wavetable Persistence**: The target signal wavetable is now cached and only rebuilt when the source signal changes, not when the number of terms changes.
- **Harmonics Pre-computation**: The app continues to pre-compute up to 250 harmonics once and reuse them for synthesis.

### 3. Instant Feedback Logic
- **Manual Fallback**: When `nTerms` changes, the high-performance synthesis cache is invalidated immediately.
- **Visuals**: The simulation heartbeats now detect the missing cache and fall back to manual summation instantly. This ensures that the visualization (circles and path) updates to the new term count with ZERO perceived latency, while the optimized cache is rebuilt in the background.

## Verification Results

### Manual Verification
- **Test**: Draw a custom shape and change `nTerms`.
- **Result**: Circles and path update instantly. No "Analyzing..." spinner appears.
- **Test**: Change Formula.
- **Result**: "Analyzing Signal..." spinner appears briefly as a new DFT is required.

### 4. Expression Compilation (AST)
- **High Performance**: The `FourierExpressionEvaluator` now compiles formulas into an Abstract Syntax Tree (AST). This makes background wavetable generation and UI-thread fallbacks significantly faster.

### 5. Interaction & Flow Cleanup
- **Manual Control**: Simulation no longer auto-starts after drawing or importing. The user must manually press "Simulate."
- **Safety**: The "Simulate" button is now disabled during the analysis/synthesis phase for all wave types, ensuring the app never tries to run before the high-performance cache is ready.
- **State Integrity**: Switching wave types immediately resets the simulation, preventing "ghosting" from the previous signal.

### 6. Unified Feedback
- **Universal Status**: The "Analyzing Signal..." message and button locking now apply to **all** signals, including standard waves (Sine, Square, etc.), providing consistent UX across the entire module.

### 7. Deferred Engine Updates (Optimized for older devices)
- **Separate UI/Engine Terms**: Introduced `intendedNTerms` (for the slider/UI) and `nTerms` (for the physics engine).
- **Deferred Processing**: Dragging the terms slider now only updates the UI label. The CPU-heavy physics engine and synthesis cache are only updated when the user **releases** the slider (`onValueChangeFinished`). This prevents CPU spikes and lag on older devices during interaction.
- **Immediate Buttons**: The +/- increment buttons still trigger immediate engine updates for precise control.

---
*Note: The system is now fully optimized for both mathematical accuracy and fluid user interaction, even on hardware with limited resources.*
