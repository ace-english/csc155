package a3;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_CONTROL_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_EVALUATION_SHADER;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Starter extends JFrame implements GLEventListener, KeyListener {
	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int texShader, axisShader;
	private int vao[] = new int[1];
	private int vbo[] = new int[13];
	private Camera camera;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private int mvLocTex, projLocTex, mvLocAxis, projLocAxis;
	private float aspect;
	private double tf;
	private boolean showAxes;

	private ImportedModel tableObj;
	private int woodTex;
	private Dictionary<String, Integer> vboDict;

	public Starter() {
		setTitle("Assignment 3");
		setSize(1000, 600);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		this.addKeyListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	@Override
	public void keyPressed(KeyEvent event) {
		int e = event.getKeyCode();
		switch (e) {
		case KeyEvent.VK_W:
			camera.moveIn();
			break;
		case KeyEvent.VK_S:
			camera.moveOut();
			break;
		case KeyEvent.VK_A:
			camera.moveLeft();
			break;
		case KeyEvent.VK_D:
			camera.moveRight();
			break;
		case KeyEvent.VK_E:
			camera.moveDown();
			break;
		case KeyEvent.VK_Q:
			camera.moveUp();
			break;
		case KeyEvent.VK_KP_UP:
		case KeyEvent.VK_UP:
			camera.pitchUp();
			break;
		case KeyEvent.VK_KP_DOWN:
		case KeyEvent.VK_DOWN:
			camera.pitchDown();
			break;
		case KeyEvent.VK_KP_LEFT:
		case KeyEvent.VK_LEFT:
			camera.panLeft();
			break;
		case KeyEvent.VK_KP_RIGHT:
		case KeyEvent.VK_RIGHT:
			camera.panRight();
			break;
		case KeyEvent.VK_SPACE:
			toggleAxes();
			break;
		case KeyEvent.VK_L:
			toggleLight();
			break;

		}

	}

	@Override
	public void keyReleased(KeyEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent event) {

	}

	private void toggleAxes() {
		showAxes = !showAxes;

	}

	private void toggleLight() {
		// TODO Auto-generated method stub

	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		gl.glUseProgram(texShader);

		mvLocTex = gl.glGetUniformLocation(texShader, "mv_matrix");
		projLocTex = gl.glGetUniformLocation(texShader, "proj_matrix");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(projLocTex, 1, false, pMat.get(vals));

		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.lookAlong(camera.getN(), camera.getV());
		mvStack.translate(new Vector3f(camera.getLocation()).negate());

		tf = elapsedTime / 1000.0; // time factor

		gl.glUseProgram(axisShader);
		mvLocAxis = gl.glGetUniformLocation(axisShader, "mv_matrix");
		projLocAxis = gl.glGetUniformLocation(axisShader, "proj_matrix");
		gl.glUniformMatrix4fv(projLocAxis, 1, false, pMat.get(vals));
		// ---------------------- axis
		if (showAxes) {

			gl.glUseProgram(axisShader);
			mvStack.pushMatrix();
			mvStack.scale(10f, 10f, 10f);
			gl.glUniformMatrix4fv(mvLocAxis, 1, false, mvStack.get(vals));
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("axisPositions")]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINES, 0, 287);
			mvStack.popMatrix(); // print axes
		}

		// use texture shader
		gl.glUseProgram(texShader);

		// table
		mvStack.translate(0.0f, 0.0f, 0.0f);
		// mvStack.rotate((float) tf, 1.0f, 0.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLocTex, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("tablePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("tableTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, woodTex);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, tableObj.getNumVertices());

		mvStack.popMatrix(); // final pop

	}

	public void init(GLAutoDrawable drawable) {

		// initialize objects
		startTime = System.currentTimeMillis();
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		vboDict = new Hashtable<String, Integer>();
		camera = new Camera();
		showAxes = false;

		// load assets
		texShader = createShaderProgram("src/a3/texVertShader.glsl", "src/a3/texFragShader.glsl");
		axisShader = createShaderProgram("src/a3/axisVertShader.glsl", "src/a3/axisFragShader.glsl");

		woodTex = loadTexture("assets/wood.jpg");
		tableObj = new ImportedModel("assets/table.obj");
		setupVertices();

	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		float[] axisPositions = { 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f, -1.0f };

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		addToVbo(gl, tableObj, "table");

		vboDict.put("axisPositions", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("axisPositions")]);
		FloatBuffer axisBufShuttle = Buffers.newDirectFloatBuffer(axisPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, axisBufShuttle.limit() * 4, axisBufShuttle, GL_STATIC_DRAW);
	}

	private void addToVbo(GL4 gl, ImportedModel obj, String name) {
		float[] pvalues = new float[obj.getNumVertices() * 3];
		float[] tvalues = new float[obj.getNumVertices() * 2];
		float[] nvalues = new float[obj.getNumVertices() * 3];

		for (int i = 0; i < obj.getNumVertices(); i++) {
			pvalues[i * 3] = (float) (obj.getVertices()[i]).x();
			pvalues[i * 3 + 1] = (float) (obj.getVertices()[i]).y();
			pvalues[i * 3 + 2] = (float) (obj.getVertices()[i]).z();
			tvalues[i * 2] = (float) (obj.getTexCoords()[i]).x();
			tvalues[i * 2 + 1] = (float) (obj.getTexCoords()[i]).y();
			nvalues[i * 3] = (float) (obj.getNormals()[i]).x();
			nvalues[i * 3 + 1] = (float) (obj.getNormals()[i]).y();
			nvalues[i * 3 + 2] = (float) (obj.getNormals()[i]).z();
		}

		vboDict.put(name + "Positions", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Positions")]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

		vboDict.put(name + "Textures", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Textures")]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

	}

	public static void main(String[] args) {
		new Starter();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void dispose(GLAutoDrawable drawable) {
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

	private static int loadTexture(String textureFileName) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		try {
			tex = TextureIO.newTexture(new File(textureFileName), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
			float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}
}
