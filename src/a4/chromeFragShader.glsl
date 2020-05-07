#version 430

in vec3 varyingNormal;
in vec3 varyingVertPos;
in vec3 vertEyeSpacePos;
out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
layout (binding = 0) uniform samplerCube t;

void main(void)
{
	vec3 r = -reflect(normalize(-varyingVertPos), normalize(varyingNormal));
	vec4 color = texture(t,r);
	vec4 fogColor = vec4(0.0, 0.0, 0.1, 1.0);	// dark blue
	float fogStart = 5;
	float fogEnd = 10;
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	fragColor = mix(fogColor,color,fogFactor);
}