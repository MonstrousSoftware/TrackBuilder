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
T for terrain editor
Terrain edit mode:
    Minus/Plus(equals) to resize terrain cursor
    PgUp/PgDown to raise/lower terrain

To do:
- terrain normals
- UI changes

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `teavm`: Web backend that supports most JVM languages.
