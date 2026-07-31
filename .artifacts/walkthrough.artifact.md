# Walkthrough - Fourier Series Export

I have implemented an "Export" feature that allows you to copy the mathematical formulas for your current Fourier series (both normal and complex forms) directly from the simulation.

## Changes Made

### Mathematical Generation

#### [NEW] [FourierExportLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierExportLogic.kt)
- **Series Formatting**: Created a logic helper to generate human-readable string representations of the Fourier series.
- **Normal Form**: Supports 1D waves ($f(t) = \sum A \cos(\dots)$) and 2D parametric curves ($x(t), y(t)$).
- **Complex Form**: Generates the exponential form using Euler's formula ($e^{i\theta}$).
- **Intelligent Filtering**: Automatically filters out terms with negligible amplitude to keep the formula concise.

### UI Integration

#### [FourierComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierComponents.kt)
- **Export Button**: Added an "Export" button next to the "Reset" button in the Decomposition boxes.
- **Interactive Dialog**: Implemented a popup window that displays both formula versions in monospaced fields.
- **Clipboard Support**: Integrated "Copy" buttons for each field using the system clipboard.
- **Dynamic Updates**: The exported formulas automatically reflect any changes you make to the harmonics via the edit sliders.

## Verification Results

- **1D Mode**: Verified that exporting a Square wave produces the correct trigonometric series with odd harmonics (1, 3, 5...).
- **2D Mode**: Verified that complex shapes (like SVG or Draw 2D) produce parametric $x(t)$ and $y(t)$ formulas.
- **Copy/Paste**: Confirmed that the "Copy" button correctly transfers the text to the system clipboard for use in other apps.

> [!TIP]
> You can use these exported formulas in graphing software like Desmos or in mathematical tools like WolframAlpha to further analyze your signals!
