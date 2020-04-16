#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 vertNormal;
layout (location = 3) in vec3 vertTangent;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 originalVertex;
out vec2 tc;

layout (binding=0) uniform sampler2D s;
layout (binding=1) uniform sampler2D t;

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
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;

void main(void)
{	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	tc = texCoord;
	originalVertex = vertPos;
	
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;

	shadow_coord = shadowMVP * vec4(vertPos,1.0);
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}