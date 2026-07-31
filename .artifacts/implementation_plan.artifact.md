# Implementation Plan - Fix SVG Input and UI

The user reported that "svg input does not work". Investigation revealed two main issues:
1. **Parser Fragility**: The SVG parser in `FourierLogic.kt` is extremely restrictive. It throws an exception if it encounters any tag not in a small whitelist (like `<circle>`, `<rect>`, or `<metadata>`), which are common in real-world SVGs. It also fails to parse paths with single quotes or commas.
2. **UI Integration**: The `svgPickerLauncher` is passed to `FourierSettingsCard` but never used or passed further down to the components that should trigger it.

## User Review Required

> [!IMPORTANT]
> I will be removing the strict tag validation that causes crashes. The app will now ignore unknown tags and extract whatever path data it can find. This is a much more robust approach for user-provided files.

## Proposed Changes

### Logic Improvements

#### [MODIFY] [FourierLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierLogic.kt)
- **Relax Tag Validation**: Remove the `IllegalArgumentException` for unknown tags.
- **Robust Attribute Parsing**: Update `dPattern` to support both `d="..."` and `d='...'`.
- **Better Tokenization**: Update `tokenRegex` to explicitly handle (or skip) commas between coordinates.
- **Improved MoveTo (`m/M`)**: Correctly handle implicit line commands for multiple coordinate pairs following a MoveTo command.

### UI Integration

#### [MODIFY] [FourierSettings.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettings.kt)
- Pass `svgPickerLauncher` to `WaveTypeSelector` and `SVGSettings`.

#### [MODIFY] [FourierSettingsComponents.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierSettingsComponents.kt)
- **`WaveTypeSelector`**: Update signature to accept `svgPickerLauncher`. Launch the picker when `WaveType.SVG` is selected or clicked.
- **`SVGSettings`**: Update signature to accept `svgPickerLauncher`. Add an "Import/Change SVG" button to allow users to pick a different file.

## Verification Plan

### Automated Tests
- I will create a scratch script `verify_svg_fix.kt` to test the updated `extractPointsFromSVG` with:
    - SVGs containing `<circle>`, `<metadata>`, etc.
    - Paths with commas and single quotes.
    - Paths with scientific notation.

### Manual Verification
- Deploy the app.
- Select "Import SVG" in the Signal Settings.
- Verify the file picker opens.
- Import a sample SVG and verify it renders in the preview and simulation.
