#version 430
uniform float Tx;
uniform float Ty;

out vec3 varyingColor;
layout (location=0) in vec3 position;
layout (location=1) in vec3 color;

void main(void){
	//gl_Position = vec4(position.x+Tx, position.y+Ty, position.z, 1.0);
	if (gl_VertexID == 0) {
		gl_Position = vec4( 0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
		varyingColor=vec3(1.0,0.0,0.0);
		}
	else if (gl_VertexID == 1){
		gl_Position = vec4(-0.25 + Tx, -0.25 + Ty, 0.0, 1.0);
		varyingColor=vec3(0.0,1.0,0.0);
		}
	else{
	 gl_Position = vec4(0.25 + Tx, 0.1 + Ty, 0.0, 1.0);
		varyingColor=vec3(0.0,0.0,1.0);
  	}
}