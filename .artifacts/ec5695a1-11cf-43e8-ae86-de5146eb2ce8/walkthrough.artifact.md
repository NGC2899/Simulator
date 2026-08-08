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

---
*Note: The performance gain is most noticeable on lower-end devices where re-generating the ideal wavetable on every slider move could cause micro-stutters.*
