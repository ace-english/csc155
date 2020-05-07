package a4;

import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_CONTROL_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_EVALUATION_SHADER;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import org.joml.Matrix4f;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

public class Shader {
	private int shader;
	private GL4 gl;

	public Shader(GL4 gl, String VertFile, String FragFile) {
		shader = createShaderProgram("src/a4/phongVertShader.glsl", "src/a4/phongFragShader.glsl");
		this.gl = gl;
	}

	private void installLights(Matrix4f mv) {

	}

	public int getShader() {
		return shader;
	}

	public GL4 getGl() {
		return gl;
	}

	public void use() {
		gl.glUseProgram(shader);

	}

	private void setup(GL4 gl) {
		gl.glUseProgram(shader);
		// mvLocTex = gl.glGetUniformLocation(shader, "mv_matrix");
		// projLocTex = gl.glGetUniformLocation(shader, "proj_matrix");
		// gl.glUniformMatrix4fv(mvLocTex, 1, false, mv.get(vals));
		// gl.glUniformMatrix4fv(projLocTex, 1, false, pMat.get(vals));
	}

	public void setMaterial(Material material) {
		int mambLoc = gl.glGetUniformLocation(shader, "material.ambient");
		int mdiffLoc = gl.glGetUniformLocation(shader, "material.diffuse");
		int mspecLoc = gl.glGetUniformLocation(shader, "material.specular");
		int mshiLoc = gl.glGetUniformLocation(shader, "material.shininess");
		gl.glProgramUniform4fv(shader, mambLoc, 1, material.getAmbient(), 0);
		gl.glProgramUniform4fv(shader, mdiffLoc, 1, material.getDiffuse(), 0);
		gl.glProgramUniform4fv(shader, mspecLoc, 1, material.getSpecular(), 0);
		gl.glProgramUniform1f(shader, mshiLoc, material.getShininess());
	}

	private static int createShaderProgram(String vS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader = prepareShader(GL_VERTEX_SHADER, vS);
		int fShader = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		finalizeProgram(vfprogram);
		return vfprogram;
	}

	private static int finalizeProgram(int sprogram) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] linked = new int[1];
		gl.glLinkProgram(sprogram);
		// checkOpenGLError();
		gl.glGetProgramiv(sprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] != 1) {
			System.out.println("linking failed");
			// printProgramLog(sprogram);
		}
		return sprogram;
	}

	private static int prepareShader(int shaderTYPE, String shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] shaderCompiled = new int[1];
		String shaderSource[] = readShaderSource(shader);
		int shaderRef = gl.glCreateShader(shaderTYPE);
		gl.glShaderSource(shaderRef, shaderSource.length, shaderSource, null, 0);
		gl.glCompileShader(shaderRef);
		// checkOpenGLError();
		gl.glGetShaderiv(shaderRef, GL_COMPILE_STATUS, shaderCompiled, 0);
		if (shaderCompiled[0] != 1) {
			if (shaderTYPE == GL_VERTEX_SHADER)
				System.out.print("Vertex ");
			if (shaderTYPE == GL_TESS_CONTROL_SHADER)
				System.out.print("Tess Control ");
			if (shaderTYPE == GL_TESS_EVALUATION_SHADER)
				System.out.print("Tess Eval ");
			if (shaderTYPE == GL_GEOMETRY_SHADER)
				System.out.print("Geometry ");
			if (shaderTYPE == GL_FRAGMENT_SHADER)
				System.out.print("Fragment ");
			System.out.println("shader compilation error.");
			// printShaderLog(shaderRef);
		}
		return shaderRef;
	}

	private static String[] readShaderSource(String filename) {
		Vector<String> lines = new Vector<String>();
		Scanner sc;
		String[] program;
		try {
			sc = new Scanner(new File(filename));
			while (sc.hasNext()) {
				lines.addElement(sc.nextLine());
			}
			program = new String[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				program[i] = (String) lines.elementAt(i) + "\n";
			}
		} catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
		return program;
	}
}
