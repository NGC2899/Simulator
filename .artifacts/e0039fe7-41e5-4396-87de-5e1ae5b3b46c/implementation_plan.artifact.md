# Implementation Plan - High-Precision Microsoft Icons (Manual)

Due to current build environment restrictions (Offline Mode), external libraries like `io.github.niyajali:fluentui-system-icons` cannot be downloaded. Instead, I will provide a high-precision, optimized manual implementation of the required Fluent UI icons.

## Proposed Changes

### [MODIFY] [FluentIcons.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/FluentIcons.kt)

Refactor the manual implementation to support the four variants requested, using a shared coordinate system and helper functions for precision.

- **Apps Icon**: Support `Color`, `Regular` (Outline), and `Filled`.
- **Other Feature Icons**: Implement `MicRegular`, `CubeRegular`, `BranchRegular`, and `SettingsRegular` using verified SVG paths from the Fluent library.
- **Switching Logic**: Maintain the current structure that allows switching by simply changing the `imageVector` property.

## Verification Plan

### Automated Tests
- Run `assembleDebug` to ensure all `ImageVector` definitions are valid and compile correctly.

### Manual Verification
1.  **Welcome Screen**: Verify all cards display the correct Microsoft icon.
2.  **Top Navigation Bar**: Confirm the Apps icon is vibrant and correctly sized.
3.  **Visual Quality**: Confirm icons are perfectly round and symmetric (addressing the "disorder" feedback).
