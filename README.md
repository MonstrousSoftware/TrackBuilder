# TrackBuilder

Test of constructing a racetrack model from a spline which is defined by a few control points.

It can include height differences by using different Y values for the control points.

Assumes a closed loop, this is easy to adapt.
With very sharp corners the texture may get distorted. Avoid those.
The centre line is modeled as a series of white rectangles floating slightly above the road surface to avoid Z-fighting.

A tilt angle for the road can be defined with a separate list of control points with the normal vector.

Press 1 for map view, press 2 for first person view.

SHIFT+Left mouse button to place a marker
X to delete the selected marker.
Arrow keys to move the selected (yellow) marker in the horizontal plane.
SHIFT + Arrow keys to rotate the selected (yellow) marker.
W to toggle wire frame mode
S to save track
L to load track
T to toggle terrain edit mode

Terrain edit mode:

- scroll wheel to change size of terrain brush
- \[ to increase size of terrain brush
- \] to decrease size of terrain brush
- shift + \[ or \]  for fine control of brush size
- brush behaviour depends on brush mode: up/down, erase, flatten, smooth

up/down mode:
- left mouse button to raise terrain
- shift + left mouse button to lower terrain

erase mode:
- terrain is changed to height zero

flatten mode:
- terrain is changed to height of selected spot

smooth mode:
- terrain height is averaged in the brush area

When in terrain edit mode:
- hold ALT key to rotate view with the mouse or zoom the view with the scroll wheel


## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `teavm`: Web backend that supports most JVM languages.
