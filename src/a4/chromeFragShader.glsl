#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingTangent;
in vec3 originalVertex;
in vec3 varyingHalfVector;
in vec3 vertEyeSpacePos;
out vec4 fragColor;


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
layout (binding = 0) uniform samplerCube t;


vec3 calcNewNormal(){
	vec3 normal = normalize(varyingNormal);
//	vec3 tangent = normalize(varyingTangent);
//	tangent = normalize(tangent - dot(tangent, normal) * normal);
//	vec3 bitangent = cross(tangent, normal);
//	mat3 tbn = mat3(tangent, bitangent, normal);
//	vec3 retrievedNormal = texture(s,tc).xyz;
//	retrievedNormal = retrievedNormal * 2.0 - 1.0;
//	vec3 newNormal = tbn * retrievedNormal;
//	newNormal = normalize(newNormal);
//	vec3 newNormal=normalize(varyingNormal)
	return normal;
}

void main(void)
{
	vec3 L = normalize(varyingLightDir);
	vec3 V = normalize(-varyingVertPos);
	
	vec3 r = -reflect(normalize(-varyingVertPos), normalize(varyingNormal));
	vec4 color = texture(t,r);
	vec4 fogColor = vec4(0.0, 0.0, 0.1, 1.0);	// dark blue
	float fogStart = 5;
	float fogEnd = 10;
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	fragColor = mix(fogColor,color,fogFactor);
}