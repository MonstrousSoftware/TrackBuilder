
// attributes of this vertex
attribute vec4 a_position;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform vec4 u_cameraPosition;

uniform int u_heightMapSize;    // in vertices
uniform float u_scale;
uniform float u_amplitude;

varying vec2 v_UV;
varying float v_fog;

void main() {
	vec4 worldPos = u_worldTrans * a_position;

    float terrainWorldSize = u_heightMapSize * u_scale;

    // offset by 0.5 because terrain is centred on origin
    v_UV = (worldPos.xz / terrainWorldSize) + vec2(0.5);


    vec3 flen = u_cameraPosition.xyz - worldPos.xyz;
    float fog = dot(flen, flen) * u_cameraPosition.w;
    v_fog = min(fog, 1.0);

   	gl_Position = u_projViewTrans * worldPos;
}
