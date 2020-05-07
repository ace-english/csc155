#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 vertEyeSpacePos;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
layout (binding = 0) uniform samplerCube t;


void main(void)
{
	vec4 p = vec4(vertPos,1.0);
	vertEyeSpacePos = (mv_matrix*p).xyz;
	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
