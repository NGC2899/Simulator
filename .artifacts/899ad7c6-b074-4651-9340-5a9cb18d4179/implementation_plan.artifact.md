# Implementation Plan - Fix SVG Input

The user reported that "svg input does not work!". Based on the investigation, the current SVG parsing implementation in `FourierLogic.kt` is extremely fragile. It throws a fatal exception when encountering common SVG tags like `<circle>`, `<rect>`, or `<title>`, and it fails to parse path data that uses single quotes or contains certain valid separators.

## User Review Required

> [!WARNING]
> The current parser crashes on almost any real-world SVG file (e.g., from Inkscape or Illustrator) because it rejects any tags other than a small whitelist. I will be changing this to a "fail-soft" approach where it ignores unknown tags and extracts all available path data.

## Proposed Changes

### Fourier Logic

#### [MODIFY] [FourierLogic.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/fourier/FourierLogic.kt)
- **Relax Tag Validation**: Remove the `IllegalArgumentException` thrown for non-whitelisted tags. The parser will now scan the entire file for path data without crashing on metadata or basic shapes (though it still only extracts path data).
- **Robust Path Regex**: Update the `d` attribute regex to support both double (`"`) and single (`'`) quotes.
- **Improved Tokenization**: Enhance the `tokenRegex` to handle commas and whitespace more robustly as coordinate separators.
- **Command Support**:
    - Ensure relative `m` (MoveTo) commands correctly treat subsequent coordinate pairs as implicit `l` (LineTo) commands.
    - Add basic consumption for smooth curve commands (`s`, `t`) so they don't break the parser's state, even if they aren't fully interpolated yet (treating them as lines for now is better than ignoring them or failing).
- **Point Normalization**: Fix a potential division by zero in `resamplePath` when `totalLength` is very small.

## Verification Plan

### Automated Tests
- I will verify the logic using a scratch script that tests:
    - SVGs with `<circle>` and `<metadata>` tags (must not crash).
    - Path data with commas: `M10,20L30,40`.
    - Path data with single quotes: `d='M 0 0 L 10 10'`.
    - Path data with scientific notation: `1.2e-3`.

### Manual Verification
- Deploy the app to the device.
- Navigate to the Fourier Series simulation.
- Attempt to import an SVG (using a known good sample string if possible).
