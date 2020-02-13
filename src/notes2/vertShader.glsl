#version 430
uniform float Tx;
uniform float Ty;
// top - 2	left - 0 right - 1
void main(void){
	if (gl_VertexID == 0) 
		gl_Position = vec4( 0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
	else if (gl_VertexID == 1)
		gl_Position = vec4(-0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
	else gl_Position = vec4(0.25 + Tx, 0.1 + Ty, 0.0, 1.0);
	
}