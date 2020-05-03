#version 430

in vec3 vNormal;
in vec3 vVertPos;
in vec2 tc;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
layout (binding=0) uniform sampler2D samp;

void main(void){
		color = texture(samp, tc);
}
