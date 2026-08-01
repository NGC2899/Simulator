# Walkthrough - UI Polish and Export Improvements

I have resolved the issue where the Export and Reset buttons disappeared and improved the Export dialog's usability.

## Changes Made

### 1. Fix Button Visibility
- **Root Cause**: The title row height was accidentally set to a fixed `16.dp` (`AppDesign.radiusLarge`), which was too small to contain the text and buttons, causing them to be clipped and invisible.
- **Fix**: Updated the title row in both `HarmonicComponents` and `ComplexHarmonicComponents` to use `heightIn(min = 40.dp)`. This ensures there is always enough space for the Export and Reset buttons while maintaining a stable layout.

### 2. Export Dialog Enhancements
- **Vertical Scrolling**: Added vertical scrolling to the formula fields in the Export dialog. This allows you to view the full series even when using many harmonics.
- **Height Constraints**: Set a maximum height of `120.dp` for the preview boxes to keep the dialog size manageable.
- **Text Selection**: Wrapped the formulas in a `SelectionContainer` so you can manually select and copy parts of the mathematical expression.

## Verification Results
- **Button Visibility**: Confirmed that the "Signal Decomposition" and "Phasor Decomposition" titles, along with the "Export" and "Reset" buttons, are now correctly visible.
- **Scroll Behavior**: Verified that long Fourier series formulas are now scrollable and don't break the dialog layout.
- **Export Functionality**: Confirmed that the Export button correctly opens the dialog with updated formulas.

> [!TIP]
> If you have a very complex drawing or SVG, you can now scroll through the exported formula to see every harmonic term!
