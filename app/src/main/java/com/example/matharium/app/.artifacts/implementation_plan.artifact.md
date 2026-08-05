# Implementation Plan - Fix Theme.kt and Add SVG Background to Welcome Screen

This plan addresses a build error in `Theme.kt` and adds a customizable SVG background to the `WelcomeScreen`.

## Proposed Changes

### [Component] UI Theme & Components

#### [MODIFY] [Theme.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Theme.kt)
- Fix the `Unresolved reference 'imageVector'` error by ensuring the parameter type is correctly capitalized as `ImageVector` in `SidebarActionButton`.
- Verify and fix any similar issues in `DisplayModeButton`.

### [Component] Welcome Screen

#### [NEW] [welcome_bg.xml](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/res/drawable/welcome_bg.xml)
- Create a placeholder SVG (Vector Drawable) that serves as the background. It will contain simple abstract paths that the user can later customize.

#### [MODIFY] [Welcome.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Welcome.kt)
- Wrap the main `Column` in `WelcomeScreen` with a `Box`.
- Insert an `Image` component using the new `welcome_bg.xml` as a background layer.
- Ensure the background is appropriately scaled to fill the screen.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the build error is resolved.

### Manual Verification
- Deploy the app and navigate to the Welcome screen to see the new background.
- Verify that the background doesn't interfere with the interactive cards.
