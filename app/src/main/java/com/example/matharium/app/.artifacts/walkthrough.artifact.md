# Walkthrough - Adjusted Welcome Screen Background

I have adjusted the background image on the Welcome Screen as requested.

## Changes Made

### Background Image Refinement
- **Source Updated**: Switched to using `R.drawable.welcome_picture` (the new SVG you added).
- **Positioning**: Moved the image to the right side of the screen using `Alignment.CenterEnd` and a slight horizontal `offset` to create a modern, partially off-screen look.
- **Rotation**: Tilted the image by `-15` degrees to give it a more dynamic feel.
- **Styling**: Increased the size and lowered the `alpha` (0.12f) to ensure it acts as a subtle background watermark without distracting from the main content.

### Implementation Details
- Modified [Welcome.kt](file:///C:/Users/Yasin/AndroidStudioProjects/Matharium/app/src/main/java/com/example/matharium/app/Welcome.kt) in the `WelcomeScreen` composable.

> [!TIP]
> You can find the adjustment logic in `Welcome.kt` around line 55. Feel free to tweak the `offset` and `rotate` values if you want a different tilt or position!

## Verification Results
- The layout remains responsive.
- The background element is correctly layered behind the interactive cards.
- Code builds and runs successfully.
