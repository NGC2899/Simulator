# Fix Graph Mode Synchronization and Coordinate System

The Graph mode (state space plot of θ1 vs θ2) currently has two issues:
1. The Y-axis is inverted (standard screen coordinates), causing the plotted trails to look "upside down" compared to mathematical conventions, which can be confusing.
2. The current position dots are not synchronized with the trails because they were calculated differently and didn't correctly use the simulation state while running.

## Proposed Changes

### [DoublePendulum.kt](file:///C:/Users/Yasin/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/DoublePendulum.kt)

#### Normalize Graph Coordinate System
- Update the trail recording to use a consistent "Math-friendly" coordinate system where Y increases upwards (by inverting the θ2 value when storing or drawing).
- Update the drawing logic to consistently apply this inversion to both the trails and the current position dots.

#### Fix Synchronization and State Tracking
- Ensure the current position dot uses the simulation state (`p.logic.thetaOne`, `p.logic.thetaTwo`) when running.
- Ensure dragging logic in Graph mode is also updated to account for the Y-axis inversion.

```diff
@@ -762,11 +762,13 @@
                                 } else if (displayMode == DisplayMode.GRAPH) {
                                     val graphScale = scale * 0.004f
                                     for (p in pendulums.asReversed()) {
-                                        val currentPos = Offset(
-                                            p.t1.toFloatOrNull() ?: 0f,
-                                            p.t2.toFloatOrNull() ?: 0f
-                                        )
-                                        if ((touch - currentPos * graphScale).getDistance() < AppDesign.sidebarButtonSize.toPx() * 0.8f) {
+                                        val cT1 = if (hasStarted) (p.logic.thetaOne * 180.0 / PI).toFloat() else (p.t1.toFloatOrNull() ?: 0f)
+                                        val cT2 = if (hasStarted) (p.logic.thetaTwo * 180.0 / PI).toFloat() else (p.t2.toFloatOrNull() ?: 0f)
+
+                                        // Apply Y-inversion for matching visual position
+                                        val visualPos = Offset(cT1, -cT2) * graphScale
+
+                                        if ((touch - visualPos).getDistance() < AppDesign.sidebarButtonSize.toPx() * 0.8f) {
                                             draggingPendulumId = p.id
                                             draggingBobType = DragTarget.ANGLE_DOT
                                             hasStarted = false
@@ -783,8 +785,11 @@
                                 if (displayMode == DisplayMode.GRAPH) {
                                     val graphScale = scale * 0.004f
                                     val touchScaled = touch / graphScale
-                                    p.t1 =
-                                        String.format(Locale.US, "%.1f", touchScaled.x.toDouble())
-                                    p.t2 =
-                                        String.format(Locale.US, "%.1f", touchScaled.y.toDouble())
+                                    p.t1 = String.format(Locale.US, "%.1f", touchScaled.x.toDouble())
+                                    // Invert Y back to math coordinates
+                                    p.t2 = String.format(Locale.US, "%.1f", -touchScaled.y.toDouble())
                                 }
                             },
@@ -813,11 +818,11 @@
                                         val start = p.angleTrail.first() * graphScale
-                                        moveTo(start.x, start.y)
+                                        moveTo(start.x, -start.y) // Invert Y
                                         for (i in 1 until p.angleTrail.size) {
                                             val point = p.angleTrail[i] * graphScale
-                                            lineTo(point.x, point.y)
+                                            lineTo(point.x, -point.y) // Invert Y
                                         }
                                     }
@@ -825,18 +830,13 @@

                                 // Current position dot
-                                val currentT1 =
-                                    if (hasStarted) (p.logic.thetaOne * 180.0 / PI).toFloat() else (p.t1.toFloatOrNull()
-                                        ?: 0f)
-                                val currentT2 =
-                                    if (hasStarted) (p.logic.thetaTwo * 180.0 / PI).toFloat() else (p.t2.toFloatOrNull()
-                                        ?: 0f)
+                                val cT1 = if (hasStarted) (p.logic.thetaOne * 180.0 / PI).toFloat() else (p.t1.toFloatOrNull() ?: 0f)
+                                val cT2 = if (hasStarted) (p.logic.thetaTwo * 180.0 / PI).toFloat() else (p.t2.toFloatOrNull() ?: 0f)
+
                                 drawCircle(
                                     p.currentColor,
-                                    AppDesign.radiusSmall.toPx() * 0.75f,
-                                    Offset(currentT1, currentT2) * graphScale
+                                    AppDesign.radiusSmall.toPx() * 0.5f,
+                                    Offset(cT1, -cT2) * graphScale // Invert Y
                                 )
                             }
```

## Verification Plan

### Automated Tests
- N/A (UI-based logic)

### Manual Verification
1. Open the application and switch to **Graph Mode**.
2. Start the simulation and verify that the colored dots follow the tips of the trails exactly.
3. Observe that the trails "go up" when θ2 increases positively (Math convention).
4. Pause the simulation and verify that dragging a dot updates the initial angles correctly.
5. Zoom in/out and verify that synchronization is maintained.
