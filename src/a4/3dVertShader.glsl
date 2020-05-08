#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
out vec3 varyingNormal;
out vec3 originalPosition;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 vertEyeSpacePos;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 texRot_matrix;

layout (binding=0) uniform sampler3D s;

void main(void)
{
	vec4 p = vec4(position,1.0);	
	varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	originalPosition = vec3(texRot_matrix * vec4(position,1.0)).xyz;
	varyingVertPos = (mv_matrix * vec4(position,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	
	vertEyeSpacePos = (mv_matrix*p).xyz;
	
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
}