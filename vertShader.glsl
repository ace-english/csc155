#version 430
uniform float Tx;
uniform float Ty;

out vec4 varyingColor;
layout (location=0) in vec3 position;

// top - 2	left - 0 right - 1
void main(void){
	if (gl_VertexID == 0) 
		gl_Position = vec4( 0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
	else if (gl_VertexID == 1)
		gl_Position = vec4(-0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
	else gl_Position = vec4(0.0 + Tx, 0.1 + Ty, 0.0, 1.0);
	
	varyingColor=vec4(position, 1.0)* 0.5 + vec4(0.5, 0.5, 0.5, 0.5);
}