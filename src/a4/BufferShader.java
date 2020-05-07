package a4;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import com.jogamp.opengl.GL4;

public class BufferShader extends Shader {
	int loc;

	public BufferShader(GL4 gl, String VertFile, String FragFile) {
		super(gl, VertFile, FragFile);
		// TODO Auto-generated constructor stub
	}

	public void setup(Matrix4f mv, FloatBuffer vals) {
		use();
		loc = getGl().glGetUniformLocation(getShader(), "shadowMVP");
		refresh(mv, vals);
	}

	public void refresh(Matrix4f mv, FloatBuffer vals) {
		getGl().glUniformMatrix4fv(loc, 1, false, mv.get(vals));
	}

}