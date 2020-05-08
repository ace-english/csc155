#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 vertNormal;
layout (location = 3) in vec3 vertTangent;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingTangent;
out vec3 originalVertex;
out vec3 varyingHalfVector;
out vec2 tc;

layout (binding=0) uniform sampler3D s;


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

void main(void)
{	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	tc = texCoord;
	originalVertex = vec3(texRot_matrix * vec4(position,1.0)).xyz;
	
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	varyingTangent = (norm_matrix * vec4(vertTangent,1.0)).xyz;

	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}