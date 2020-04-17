#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 shadow_coord;
in vec2 tc;

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

layout (binding=0) uniform sampler2D t;
layout (binding=1) uniform sampler2D s;
layout (binding=2) uniform sampler2DShadow shadowTex;

void main(void)
{	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(varyingHalfVector);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	vec4 texel = texture(t,tc);
	float notInShadow=0;
	//float notInShadow = textureProj(shadowTex, shadow_coord);
		
		vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
	fragColor = texel* vec4((ambient + diffuse), 1.0)+vec4((specular), 1.0);
	
	if (notInShadow == 1.0)
	{	fragColor += light.diffuse * material.diffuse * max(dot(L,N),0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0);
	}
	
}