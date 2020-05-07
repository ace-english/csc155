package a4;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import com.jogamp.opengl.GL4;

public class FlatShader extends Shader {
	int mvLocTex;
	int projLocTex;

	public FlatShader(GL4 gl, String VertFile, String FragFile) {
		super(gl, VertFile, FragFile);
	}

	public void setup(Matrix4f mv, Matrix4f pMat, FloatBuffer vals) {
		use();
		mvLocTex = getGl().glGetUniformLocation(getShader(), "mv_matrix");
		projLocTex = getGl().glGetUniformLocation(getShader(), "proj_matrix");
		refreshMV(mv, vals);
		refreshProj(pMat, vals);
	}

	public void refreshMV(Matrix4f mv, FloatBuffer vals) {
		getGl().glUniformMatrix4fv(mvLocTex, 1, false, mv.get(vals));
	}

	public void refreshProj(Matrix4f pMat, FloatBuffer vals) {
		getGl().glUniformMatrix4fv(mvLocTex, 1, false, pMat.get(vals));
	}
}
