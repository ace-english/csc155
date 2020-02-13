#version 430
out vec4 color;
in vec3 varyingColor;
uniform bool rainbow;
void main(void){ 
	if(rainbow){
		color = vec4(varyingColor,1.0);
	}
	else{
		color = vec4(0.0, 1.0, 1.0, 1.0);
	}
}