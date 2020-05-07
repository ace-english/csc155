package a4;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

public class LightingShader extends FlatShader {
	int nLoc;
	int sLoc;

	public LightingShader(GL4 gl, String VertFile, String FragFile) {
		super(gl, VertFile, FragFile);
	}

	public void setup(Matrix4f mv, Matrix4f pMat, Matrix4f shadowMVP, Matrix4f invTr, FloatBuffer vals) {
		super.setup(mv, pMat, vals);
		nLoc = getGl().glGetUniformLocation(getShader(), "norm_matrix");
		sLoc = getGl().glGetUniformLocation(getShader(), "shadowMVP");
		refreshShadow(shadowMVP, vals);
		refreshNormals(invTr, vals);
	}

	public void refreshShadow(Matrix4f shadowMVP, FloatBuffer vals) {
		getGl().glUniformMatrix4fv(sLoc, 1, false, shadowMVP.get(vals));
	}

	public void refreshNormals(Matrix4f invTr, FloatBuffer vals) {
		getGl().glUniformMatrix4fv(nLoc, 1, false, invTr.get(vals));
	}

	public void installLights(Matrix4f vMatrix, PositionalLight light, GlobalAmbientLight ambientLight) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		Vector3f currentLightPos = new Vector3f(light.getPosition());
		currentLightPos.mulPosition(vMatrix);
		float[] lightPos = new float[3];
		lightPos[0] = currentLightPos.x();
		lightPos[1] = currentLightPos.y();
		lightPos[2] = currentLightPos.z();

		// get the locations of the light and material fields in the shader
		int globalAmbLoc = gl.glGetUniformLocation(getShader(), "globalAmbient");
		int ambLoc = gl.glGetUniformLocation(getShader(), "light.ambient");
		int diffLoc = gl.glGetUniformLocation(getShader(), "light.diffuse");
		int specLoc = gl.glGetUniformLocation(getShader(), "light.specular");
		int posLoc = gl.glGetUniformLocation(getShader(), "light.position");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(getShader(), globalAmbLoc, 1, ambientLight.getAmbient(), 0);
		gl.glProgramUniform4fv(getShader(), ambLoc, 1, light.getAmbient(), 0);
		gl.glProgramUniform4fv(getShader(), diffLoc, 1, light.getDiffuse(), 0);
		gl.glProgramUniform4fv(getShader(), specLoc, 1, light.getSpecular(), 0);
		gl.glProgramUniform3fv(getShader(), posLoc, 1, lightPos, 0);
	}

	public void uninstallLights(Matrix4f vMatrix, PositionalLight light, GlobalAmbientLight ambientLight) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		Vector3f currentLightPos = new Vector3f(light.getPosition());
		currentLightPos.mulPosition(vMatrix);
		float[] lightPos = new float[3];
		lightPos[0] = currentLightPos.x();
		lightPos[1] = currentLightPos.y();
		lightPos[2] = currentLightPos.z();

		// get the locations of the light and material fields in the shader
		int globalAmbLoc = gl.glGetUniformLocation(getShader(), "globalAmbient");
		int ambLoc = gl.glGetUniformLocation(getShader(), "light.ambient");
		int diffLoc = gl.glGetUniformLocation(getShader(), "light.diffuse");
		int specLoc = gl.glGetUniformLocation(getShader(), "light.specular");
		int posLoc = gl.glGetUniformLocation(getShader(), "light.position");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(getShader(), globalAmbLoc, 1, ambientLight.getAmbient(), 0);
		gl.glProgramUniform4fv(getShader(), ambLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform4fv(getShader(), diffLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform4fv(getShader(), specLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform3fv(getShader(), posLoc, 1, lightPos, 0);
	}

	public void setMaterial(Material material) {
		int mambLoc = getGl().glGetUniformLocation(getShader(), "material.ambient");
		int mdiffLoc = getGl().glGetUniformLocation(getShader(), "material.diffuse");
		int mspecLoc = getGl().glGetUniformLocation(getShader(), "material.specular");
		int mshiLoc = getGl().glGetUniformLocation(getShader(), "material.shininess");
		getGl().glProgramUniform4fv(getShader(), mambLoc, 1, material.getAmbient(), 0);
		getGl().glProgramUniform4fv(getShader(), mdiffLoc, 1, material.getDiffuse(), 0);
		getGl().glProgramUniform4fv(getShader(), mspecLoc, 1, material.getSpecular(), 0);
		getGl().glProgramUniform1f(getShader(), mshiLoc, material.getShininess());
	}
}
