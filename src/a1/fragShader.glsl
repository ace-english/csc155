#version 430
out vec4 color;
uniform bool rainbow=true;
void main(void){ 
	if(rainbow){
		if (gl_VertexID == 0) 
			gl_Color = vec4(0.0, 1.0, 0.0, 1.0);
		else if (gl_VertexID == 1)
			gl_Color = vec4(1.0, 0.0, 0.0, 1.0);
		else [
			gl_Color = vec4(0.0, 0.0, 1.0, 1.0);
	}
	else{
		color = vec4(0.0, 1.0, 1.0, 1.0);
	}
}