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
- **High Performance**: The `FourierExpressionEvaluator` now compiles formulas into an Abstract Syntax Tree (AST) rather than parsing the string on every call. This makes background wavetable generation and UI-thread fallbacks significantly faster.

### 5. Immediate UI Synchronization
- **No Overlap Glitches**: When switching wave types or changing terms, the internal harmonics list is updated immediately. This prevents the "ghosting" effect where the simulation would show the old signal for a few hundred milliseconds while the background cache was rebuilding.
- **Target Reset**: The ideal wavetable is cleared instantly when changing modes, ensuring the error gradient doesn't calculate against stale data.

---
*Note: The combined optimizations result in a simulation that feels "native" and responsive, with zero perceived latency during interaction.*
