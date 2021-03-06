#version 430

in vec3 varyingNormal;
in vec3 originalPosition;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
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
uniform mat4 texRot_matrix;

layout (binding=0) uniform sampler3D s;

void main(void)
{	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	
	// compute light reflection vector, with respect N:
	vec3 R = normalize(reflect(-L, N));
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// angle between the view vector and reflected light:
	float cosPhi = dot(V,R);

	vec4 texel = texture(s,originalPosition/2.0+0.5);
	
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz*0.5;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);

	vec4 color = texel* vec4((ambient + diffuse), 1.0)+vec4((specular), 1.0);
	vec4 fogColor = vec4(0.0, 0.0, 0.1, 1.0);	// dark blue
	float fogStart = 5;
	float fogEnd = 10;
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	fragColor = mix(fogColor,color,fogFactor);
}
