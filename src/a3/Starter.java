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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
	private int texShader, axisShader, phongShader;
	private int vao[] = new int[1];
	private int vbo[] = new int[40];
	private Camera camera;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f invTr = new Matrix4f();
	private Matrix4f mv = new Matrix4f();
	private int mvLocTex, projLocTex, nLocTex, mvLocAxis, projLocAxis, mvLocPhong, projLocPhong, nLocPhong;
	private float aspect;
	private double tf;
	private boolean showAxes, showLight;
	private int[] mouseDragCurrent;
	private Vector3f absoluteLightPos;

	private ImportedModel tableObj, scrollObj, bagObj, keyObj, coinObj, bookObj;
	private Sphere lightObj;
	private int woodTex, scrollTex, burlapTex, metalTex, yellowTex;
	private Material goldMat, pewterMat;
	private Light globalAmbientLight;
	private PositionalLight mouseLight;
	private Dictionary<String, Integer> vboDict;

	MouseAdapter myMouseAdapter = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent event) {
			mouseDragCurrent = new int[] { event.getX(), event.getY() };
			System.out.printf("initial: (%d,%d)\n", mouseDragCurrent[0], mouseDragCurrent[1]);
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			System.out.println("mouseDragged: (" + event.getX() + "," + event.getY() + ")");
			float[] vector = new float[] { event.getX() - mouseDragCurrent[0], event.getY() - mouseDragCurrent[1] };
			System.out.printf("dragging: (%f,%f)\n", vector[0], vector[1]);
			mouseLight.getPosition().add(vector[0] * .0003f, vector[1] * -.0003f, 0f);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			System.out.println("scroll: " + event.getWheelRotation());
			mouseLight.getPosition().add(0f, 0f, event.getWheelRotation() * -.03f);

		}

	};

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
		myCanvas.addKeyListener(this);
		this.addMouseListener(myMouseAdapter);
		myCanvas.addMouseListener(myMouseAdapter);
		this.addMouseMotionListener(myMouseAdapter);
		myCanvas.addMouseMotionListener(myMouseAdapter);
		this.addMouseWheelListener(myMouseAdapter);
		myCanvas.addMouseWheelListener(myMouseAdapter);
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
		case KeyEvent.VK_NUMPAD0:
			resetLight();
			break;

		}

	}

	@Override
	public void keyReleased(KeyEvent event) {

	}

	@Override
	public void keyTyped(KeyEvent event) {

	}

	private void toggleAxes() {
		showAxes = !showAxes;

	}

	private void toggleLight() {
		showLight = !showLight;

	}

	private void resetLight() {
		mouseLight.setPosition(new Vector3f(0f, 3f, 0f));

	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.lookAlong(camera.getN(), camera.getV());
		mvStack.translate(new Vector3f(camera.getLocation()).negate());

		mv = new Matrix4f();
		mv = mv.mul(mvStack);
		mv.invert(invTr);
		invTr.transpose(invTr);

		gl.glUseProgram(texShader);

		mvLocTex = gl.glGetUniformLocation(texShader, "mv_matrix");
		projLocTex = gl.glGetUniformLocation(texShader, "proj_matrix");
		nLocTex = gl.glGetUniformLocation(texShader, "norm_matrix");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(mvLocTex, 1, false, mv.get(vals));
		gl.glUniformMatrix4fv(projLocTex, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLocTex, 1, false, invTr.get(vals));

		gl.glUseProgram(phongShader);
		mvLocPhong = gl.glGetUniformLocation(phongShader, "mv_matrix");
		projLocPhong = gl.glGetUniformLocation(phongShader, "proj_matrix");
		nLocPhong = gl.glGetUniformLocation(phongShader, "norm_matrix");

		gl.glUniformMatrix4fv(mvLocPhong, 1, false, mv.get(vals));
		gl.glUniformMatrix4fv(projLocPhong, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLocPhong, 1, false, invTr.get(vals));

		tf = elapsedTime / 1000.0; // time factor

		// ---------------------- axis
		if (showAxes) {

			gl.glUseProgram(axisShader);
			mvLocAxis = gl.glGetUniformLocation(axisShader, "mv_matrix");
			projLocAxis = gl.glGetUniformLocation(axisShader, "proj_matrix");
			gl.glUniformMatrix4fv(projLocAxis, 1, false, pMat.get(vals));
			mvStack.pushMatrix();
			mvStack.scale(10f, 10f, 10f);
			gl.glUniformMatrix4fv(mvLocAxis, 1, false, mvStack.get(vals));
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("axisPositions")]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINES, 0, 6);
			mvStack.popMatrix(); // print axes
		}

		// use texture shader
		gl.glUseProgram(texShader);

		addToDisplay(gl, "table", woodTex, tableObj);
		addToDisplay(gl, "bag", burlapTex, bagObj);
		addToDisplay(gl, "scroll", scrollTex, scrollObj);

		// use phong shader
		gl.glUseProgram(phongShader);
		addToDisplay(gl, "coin", metalTex, coinObj);
		addToDisplay(gl, "key", metalTex, keyObj);

		// create light as child of camera
		if (showLight) {
			gl.glUseProgram(texShader);
			mvStack.pushMatrix();
			mvStack.translate(mouseLight.getPosition());
			mvStack.scale(.05f, .05f, .05f);
			addToDisplay(gl, "light", yellowTex, lightObj);
			mvStack.popMatrix();
			installLights(mv);
		} else {
			uninstallLights(mv);

		}
		mvStack.popMatrix(); // final pop

	}

	private void installLights(Matrix4f vMatrix) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		Vector3f currentLightPos = new Vector3f(mouseLight.getPosition());
		currentLightPos.mulPosition(vMatrix);
		float[] lightPos = new float[3];
		lightPos[0] = currentLightPos.x();
		lightPos[1] = currentLightPos.y();
		lightPos[2] = currentLightPos.z();

		// get the locations of the light and material fields in the shader
		int globalAmbLoc = gl.glGetUniformLocation(phongShader, "globalAmbient");
		int ambLoc = gl.glGetUniformLocation(phongShader, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(phongShader, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(phongShader, "light.specular");
		int posLoc = gl.glGetUniformLocation(phongShader, "light.position");
		int mambLoc = gl.glGetUniformLocation(phongShader, "material.ambient");
		int mdiffLoc = gl.glGetUniformLocation(phongShader, "material.diffuse");
		int mspecLoc = gl.glGetUniformLocation(phongShader, "material.specular");
		int mshiLoc = gl.glGetUniformLocation(phongShader, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(phongShader, globalAmbLoc, 1, globalAmbientLight.getAmbient(), 0);
		gl.glProgramUniform4fv(phongShader, ambLoc, 1, mouseLight.getAmbient(), 0);
		gl.glProgramUniform4fv(phongShader, diffLoc, 1, mouseLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(phongShader, specLoc, 1, mouseLight.getSpecular(), 0);
		gl.glProgramUniform3fv(phongShader, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(phongShader, mambLoc, 1, goldMat.getAmbient(), 0);
		gl.glProgramUniform4fv(phongShader, mdiffLoc, 1, goldMat.getDiffuse(), 0);
		gl.glProgramUniform4fv(phongShader, mspecLoc, 1, goldMat.getSpecular(), 0);
		gl.glProgramUniform1f(phongShader, mshiLoc, goldMat.getShininess());
	}

	private void uninstallLights(Matrix4f vMatrix) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		Vector3f currentLightPos = new Vector3f(mouseLight.getPosition());
		currentLightPos.mulPosition(vMatrix);
		float[] lightPos = new float[3];
		lightPos[0] = currentLightPos.x();
		lightPos[1] = currentLightPos.y();
		lightPos[2] = currentLightPos.z();

		// get the locations of the light and material fields in the shader
		int globalAmbLoc = gl.glGetUniformLocation(phongShader, "globalAmbient");
		int ambLoc = gl.glGetUniformLocation(phongShader, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(phongShader, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(phongShader, "light.specular");
		int posLoc = gl.glGetUniformLocation(phongShader, "light.position");
		int mambLoc = gl.glGetUniformLocation(phongShader, "material.ambient");
		int mdiffLoc = gl.glGetUniformLocation(phongShader, "material.diffuse");
		int mspecLoc = gl.glGetUniformLocation(phongShader, "material.specular");
		int mshiLoc = gl.glGetUniformLocation(phongShader, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(phongShader, globalAmbLoc, 1, globalAmbientLight.getAmbient(), 0);
		gl.glProgramUniform4fv(phongShader, ambLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform4fv(phongShader, diffLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform4fv(phongShader, specLoc, 1, new float[] { 0f, 0f, 0f }, 0);
		gl.glProgramUniform3fv(phongShader, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(phongShader, mambLoc, 1, goldMat.getAmbient(), 0);
		gl.glProgramUniform4fv(phongShader, mdiffLoc, 1, goldMat.getDiffuse(), 0);
		gl.glProgramUniform4fv(phongShader, mspecLoc, 1, goldMat.getSpecular(), 0);
		gl.glProgramUniform1f(phongShader, mshiLoc, goldMat.getShininess());
	}

	private void addToDisplay(GL4 gl, String name, int texture, WorldObject obj) {
		gl.glUniformMatrix4fv(mvLocTex, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Positions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Textures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, obj.getNumVertices());

	}

	public void init(GLAutoDrawable drawable) {

		// initialize objects
		startTime = System.currentTimeMillis();
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		vboDict = new Hashtable<String, Integer>();
		camera = new Camera();
		showAxes = false;
		showLight = true;

		goldMat = new Material(new float[] { 0.24725f, 0.1995f, 0.0745f, 1.0f },
				new float[] { 0.75164f, 0.60648f, 0.22648f, 1.0f }, new float[] { 0.62828f, 0.5558f, 0.36607f, 1.0f },
				51.2f);

		pewterMat = new Material(new float[] { .11f, .06f, .11f, 1.0f }, new float[] { .43f, .47f, .54f, 1.0f },
				new float[] { .33f, .33f, .52f, 1.0f }, 9.85f);

		globalAmbientLight = new GlobalAmbientLight();
		mouseLight = new PositionalLight(new float[] { 0.1f, 0.1f, 0.1f, 1.0f }, new float[] { 1.0f, 1.0f, 1.0f, 1.0f },
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, new Vector3f(0f, 0f, 0f));
		resetLight();

		// load assets
		texShader = createShaderProgram("src/a3/texVertShader.glsl", "src/a3/texFragShader.glsl");
		// texShader = createShaderProgram("src/a3/phongVertShader.glsl",
		// "src/a3/phongFragShader.glsl");
		axisShader = createShaderProgram("src/a3/axisVertShader.glsl", "src/a3/axisFragShader.glsl");
		phongShader = createShaderProgram("src/a3/phongVertShader.glsl", "src/a3/phongFragShader.glsl");

		woodTex = loadTexture("assets/wood.jpg");
		scrollTex = loadTexture("assets/scroll.png");
		metalTex = loadTexture("assets/metal.jpg");
		yellowTex = loadTexture("assets/coin.png");
		burlapTex = loadTexture("assets/burlap.png");

		lightObj = new Sphere();
		scrollObj = new ImportedModel("assets/scroll.obj");
		tableObj = new ImportedModel("assets/table.obj");
		bagObj = new ImportedModel("assets/bag.obj");
		keyObj = new ImportedModel("assets/key.obj");
		coinObj = new ImportedModel("assets/coin.obj");
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
		addToVbo(gl, scrollObj, "scroll");
		addToVbo(gl, keyObj, "key");
		addToVbo(gl, bagObj, "bag");
		addToVbo(gl, coinObj, "coin");

		int numSphereVerts = lightObj.getIndices().length;

		int[] indicesSphere = lightObj.getIndices();

		float[] pvaluesSphere = new float[indicesSphere.length * 3];
		float[] tvaluesSphere = new float[indicesSphere.length * 2];
		float[] nvaluesSphere = new float[indicesSphere.length * 3];

		for (int i = 0; i < indicesSphere.length; i++) {
			pvaluesSphere[i * 3] = (float) (lightObj.getVertices()[indicesSphere[i]]).x;
			pvaluesSphere[i * 3 + 1] = (float) (lightObj.getVertices()[indicesSphere[i]]).y;
			pvaluesSphere[i * 3 + 2] = (float) (lightObj.getVertices()[indicesSphere[i]]).z;
			tvaluesSphere[i * 2] = (float) (lightObj.getTexCoords()[indicesSphere[i]]).x;
			tvaluesSphere[i * 2 + 1] = (float) (lightObj.getTexCoords()[indicesSphere[i]]).y;
			nvaluesSphere[i * 3] = (float) (lightObj.getNormals()[indicesSphere[i]]).x;
			nvaluesSphere[i * 3 + 1] = (float) (lightObj.getNormals()[indicesSphere[i]]).y;
			nvaluesSphere[i * 3 + 2] = (float) (lightObj.getNormals()[indicesSphere[i]]).z;
		}

		vboDict.put("lightPositions", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightPositions")]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);

		vboDict.put("lightTextures", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightTextures")]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);

		vboDict.put("lightNormals", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightNormals")]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);

		vboDict.put("axisPositions", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("axisPositions")]);
		FloatBuffer axisBufShuttle = Buffers.newDirectFloatBuffer(axisPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, axisBufShuttle.limit() * 4, axisBufShuttle, GL_STATIC_DRAW);
	}

	private void addToVbo(GL4 gl, WorldObject obj, String name) {
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

		vboDict.put(name + "Normals", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Normals")]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);

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
