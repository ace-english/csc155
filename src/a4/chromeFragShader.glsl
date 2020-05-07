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
	vec3 tangent = normalize(varyingTangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
//	vec3 retrievedNormal = texture(s,tc).xyz;
//	retrievedNormal = retrievedNormal * 2.0 - 1.0;
//	vec3 newNormal = tbn * retrievedNormal;
//	newNormal = normalize(newNormal);
	return normal;
}

void main(void)
{
	vec3 L = normalize(varyingLightDir);
	vec3 V = normalize(-varyingVertPos);
	
	vec3 N = calcNewNormal();
	
	float cosTheta = dot(L,N);
	vec3 H = normalize(varyingHalfVector);
	
	// compute light reflection vector, with respect N:
	vec3 R = normalize(reflect(-L, N));
	
	// angle between the view vector and reflected light:
	float cosPhi = dot(V,R);
		
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
	
	vec3 r = -reflect(normalize(-varyingVertPos), normalize(varyingNormal));
	vec4 texel = texture(t,r);
	
	vec4 color = texel+vec4((ambient + diffuse), 1.0)+vec4((specular), 1.0);
	//vec4 color = globalAmbient + texel * (light.ambient + light.diffuse * max(cosTheta,0.0)+ light.specular * pow(max(cosPhi,0.0), material.shininess));
	
	vec4 fogColor = vec4(0.0, 0.0, 0.1, 1.0);	// dark blue
	float fogStart = 5;
	float fogEnd = 6;
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	fragColor = mix(fogColor,color,fogFactor);
}