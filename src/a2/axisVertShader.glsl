#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

out vec4 varyingColor;

void main(void){
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	varyingColor = vec4(position,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);

	int r=1;
	int g=1;
	int b=1;
	if(position.x==0){
		r=0;
	}if(position.y==0){
		g=0;
	}if(position.z==0){
		b=0;
	}
	varyingColor = vec4(r,g,b,1.0);


	//varyingColor = vec4(position,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);
}
