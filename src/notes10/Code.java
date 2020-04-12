package notes10;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import java.nio.FloatBuffer;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

public class Code extends JFrame implements GLEventListener {
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[4];
	private Vector3f sphereLoc = new Vector3f(0, 0, -1);
	private Vector3f cameraLoc = new Vector3f(0, 0, 1.7f);

	private Vector3f lightLoc = new Vector3f(3.0f, 2.0f, 3.0f);
	private float rotY = -2.5f;

	private Sphere mySphere = new Sphere(48);
	private int numSphereVertices;

	private int moonNormalMap;
	private int moonTexture;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f(); // perspective matrix
	private Matrix4f vMat = new Matrix4f(); // view matrix
	private Matrix4f mMat = new Matrix4f(); // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private int mvLoc, projLoc, nLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private float aspect;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];

	// white light
	float[] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

	// gold material
	float[] matAmb = Utils.silverAmbient();
	float[] matDif = Utils.silverDiffuse();
	float[] matSpe = Utils.silverSpecular();
	float matShi = Utils.silverShininess();

	public Code() {
		setTitle("Chapter10 - program3");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");

		vMat.identity().setTranslation(-cameraLoc.x(), -cameraLoc.y(), -cameraLoc.z());

		mMat.identity();
		mMat.translate(sphereLoc.x(), sphereLoc.y(), sphereLoc.z());
		mMat.rotateX((float) Math.toRadians(20.0f));
		mMat.rotateY(rotY);
		rotY += .002f;

		currentLightPos.set(lightLoc);
		installLights(vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(3);

		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glBindTexture(gl.GL_TEXTURE_2D, moonNormalMap);

		gl.glActiveTexture(gl.GL_TEXTURE1);
		gl.glBindTexture(gl.GL_TEXTURE_2D, moonTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
	}

	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = Utils.createShaderProgram("src/notes10/vertShader.glsl", "src/notes10/fragShader.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupVertices();
		moonTexture = Utils.loadTexture("assets/wood.jpg");
		moonNormalMap = Utils.loadTexture("assets/wood_normal.jpg");
	}

	private void installLights(Matrix4f vMatrix) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		currentLightPos.mulPosition(vMatrix);
		lightPos[0] = currentLightPos.x();
		lightPos[1] = currentLightPos.y();
		lightPos[2] = currentLightPos.z();

		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		numSphereVertices = mySphere.getIndices().length;

		int[] indices = mySphere.getIndices();
		Vector3f[] vertices = mySphere.getVertices();
		Vector2f[] texCoords = mySphere.getTexCoords();
		Vector3f[] normals = mySphere.getNormals();
		Vector3f[] tangents = mySphere.getTangents();

		float[] pvalues = new float[indices.length * 3];
		float[] tvalues = new float[indices.length * 2];
		float[] nvalues = new float[indices.length * 3];
		float[] tanvalues = new float[indices.length * 3];

		for (int i = 0; i < indices.length; i++) {
			pvalues[i * 3] = (float) (vertices[indices[i]]).x();
			pvalues[i * 3 + 1] = (float) (vertices[indices[i]]).y();
			pvalues[i * 3 + 2] = (float) (vertices[indices[i]]).z();
			tvalues[i * 2] = (float) (texCoords[indices[i]]).x();
			tvalues[i * 2 + 1] = (float) (texCoords[indices[i]]).y();
			nvalues[i * 3] = (float) (normals[indices[i]]).x();
			nvalues[i * 3 + 1] = (float) (normals[indices[i]]).y();
			nvalues[i * 3 + 2] = (float) (normals[indices[i]]).z();
			tanvalues[i * 3] = (float) (tangents[indices[i]]).x();
			tanvalues[i * 3 + 1] = (float) (tangents[indices[i]]).y();
			tanvalues[i * 3 + 2] = (float) (tangents[indices[i]]).z();
		}

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(4, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer tanBuf = Buffers.newDirectFloatBuffer(tanvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit() * 4, tanBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) {
		new Code();
	}

	public void dispose(GLAutoDrawable drawable) {
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}
}