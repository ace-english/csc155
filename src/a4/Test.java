package a4;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_CONTROL_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_EVALUATION_SHADER;
import static com.jogamp.opengl.GL4.*;
import java.lang.Math;
import java.awt.Color;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;
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

public class Test extends JFrame implements GLEventListener, KeyListener {
	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	Random random = new Random();
	private FlatShader texShader, axisShader;
	private LightingShader phongShader;
	private BufferShader pass1Shader;
	private int vao[] = new int[1];
	private int vbo[] = new int[50];
	private Camera camera;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f invTr = new Matrix4f();
	private Matrix4f mv = new Matrix4f();

	private float aspect;
	private double tf;
	private boolean showAxes, showLight;
	private int[] mouseDragCurrent;

	private ImportedModel tableObj, scrollObj, bagObj, keyObj, coinObj, bookPagesObj, bookCoverObj, gobletObj, gem2Obj,
			floorObj;
	private Sphere lightObj;
	private int woodTex, scrollTex, burlapTex, metalTex, leatherTex, yellowTex, skyboxTex;
	private int woodNorm, blankNorm, burlapNorm, metalNorm, leatherNorm;
	private Material goldMat, leatherMat, woodMat, burlapMat, paperMat, pewterMat, glassMat;
	private GlobalAmbientLight globalAmbientLight;
	private PositionalLight mouseLight;
	private Dictionary<String, Integer> vboDict;

	// 3D Texture variables
	private int noiseTexture;
	private int noiseHeight = 300;
	private int noiseWidth = 300;
	private int noiseDepth = 300;
	private double[][][] noise = new double[noiseHeight][noiseWidth][noiseDepth];

	// reflection/refraction variables
	private int[] bufferId = new int[1];
	private int refractTextureId;
	private int reflectTextureId;
	private int refractFrameBuffer;
	private int reflectFrameBuffer;

	// shadow-related variables
	private int screenSizeX, screenSizeY;
	private int[] shadowTex = new int[1];
	private int[] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
	private String path = "src/a4/";

	MouseAdapter myMouseAdapter = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent event) {
			mouseDragCurrent = new int[] { event.getX(), event.getY() };
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			float[] vector = new float[] { event.getX() - mouseDragCurrent[0], event.getY() - mouseDragCurrent[1] };
			mouseLight.getPosition().add(vector[0] * .0003f, vector[1] * -.0003f, 0f);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			mouseLight.getPosition().add(0f, 0f, event.getWheelRotation() * -.1f);

		}

	};

	public Test() {
		setTitle("Assignment 4");
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
		mouseLight.setPosition(new Vector3f(.01f, 2f, .01f));

	}

	void createReflectRefractBuffers() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Initialize Reflect Framebuffer
		gl.glGenFramebuffers(1, bufferId, 0);
		reflectFrameBuffer = bufferId[0];
		gl.glBindFramebuffer(GL_FRAMEBUFFER, reflectFrameBuffer);
		gl.glGenTextures(1, bufferId, 0);
		reflectTextureId = bufferId[0];
		gl.glBindTexture(GL_TEXTURE_2D, reflectTextureId);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_RGBA,
				GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, reflectTextureId, 0);
		gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
		gl.glGenTextures(1, bufferId, 0);
		gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, myCanvas.getWidth(), myCanvas.getHeight(), 0,
				GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);

		// Initialize Refract Framebuffer
		gl.glGenFramebuffers(1, bufferId, 0);
		refractFrameBuffer = bufferId[0];
		gl.glBindFramebuffer(GL_FRAMEBUFFER, refractFrameBuffer);
		gl.glGenTextures(1, bufferId, 0);
		refractTextureId = bufferId[0];
		gl.glBindTexture(GL_TEXTURE_2D, refractTextureId);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_RGBA,
				GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, refractTextureId, 0);
		gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
		gl.glGenTextures(1, bufferId, 0);
		gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, myCanvas.getWidth(), myCanvas.getHeight(), 0,
				GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);
	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		lightVmat.identity().setLookAt(mouseLight.getPosition(), origin, up); // vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		tf = elapsedTime / 1000.0; // time factor

		// flickering candlelight? Must be a sine of the times
		float mod = (float) (Math.sin(15 * tf) / (150 + (Math.random() * 150)));
		mouseLight.add(mod);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);

		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL); // for reducing
		gl.glPolygonOffset(3.0f, 5.0f); // shadow artifacts

		passOne();

		gl.glDisable(GL_POLYGON_OFFSET_FILL); // artifact reduction, continued

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);

		gl.glDrawBuffer(GL_FRONT);

		passTwo();

	}

	private void passOne() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		pass1Shader.setup(mv, vals);

		addToShadow(gl, "floor", floorObj);
		addToShadow(gl, "table", tableObj);
		addToShadow(gl, "scroll", scrollObj);
		addToShadow(gl, "bag", bagObj);
		addToShadow(gl, "coin", coinObj);
		addToShadow(gl, "key", keyObj);
		addToShadow(gl, "goblet", gobletObj);
		addToShadow(gl, "gem2", gem2Obj);
		addToShadow(gl, "bookCover", bookCoverObj);
		addToShadow(gl, "bookPages", bookPagesObj);

	}

	private void passTwo() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.lookAlong(camera.getN(), camera.getV());

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		mvStack.translate(new Vector3f(camera.getLocation()).negate());

		mv = new Matrix4f();
		mv = mv.mul(mvStack);
		mv.invert(invTr);
		invTr.transpose(invTr);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);

		phongShader.setup(mv, pMat, shadowMVP2, invTr, vals);

		// ---------------------- axis
		if (showAxes) {

			axisShader.setup(mvStack, pMat, vals);

			mvStack.pushMatrix();
			mvStack.scale(10f, 10f, 10f);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("axisPositions")]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINES, 0, 6);
			mvStack.popMatrix(); // print axes
		}

		// light
		if (showLight) {
			texShader.setup(mv, pMat, vals);
			mvStack.pushMatrix();
			mvStack.translate(mouseLight.getPosition());
			mvStack.scale(.05f, .05f, .05f);
			texShader.refreshMV(mvStack, vals);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightPositions")]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0); // pull up texture coords
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightTextures")]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2); // activate texture object
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, yellowTex);
			gl.glEnable(GL_DEPTH_TEST);

			gl.glDrawArrays(GL_TRIANGLES, 0, lightObj.getNumVertices());
			mvStack.popMatrix();
			phongShader.installLights(mv, mouseLight, globalAmbientLight);
		} else {
			phongShader.uninstallLights(mv, mouseLight, globalAmbientLight);
		}

		phongShader.use();
		addToDisplay(gl, "floor", woodTex, woodNorm, woodMat, floorObj);
		addToDisplay(gl, "table", woodTex, woodNorm, woodMat, tableObj);
		addToDisplay(gl, "scroll", scrollTex, blankNorm, paperMat, scrollObj);
		addToDisplay(gl, "bag", burlapTex, burlapNorm, burlapMat, bagObj);
		addToDisplay(gl, "coin", yellowTex, metalNorm, goldMat, coinObj); //
		addToDisplay(gl, "key", metalTex, metalNorm, pewterMat, keyObj);
		addToDisplay(gl, "bookCover", leatherTex, leatherNorm, leatherMat, bookCoverObj);
		addToDisplay(gl, "bookPages", scrollTex, blankNorm, paperMat, bookPagesObj);

		mvStack.popMatrix(); // final pop
	}

	private void addToDisplay(GL4 gl, String name, int texture, int normal, Material currentMat, WorldObject obj) {
		phongShader.refreshShadow(shadowMVP2, vals);
		phongShader.refreshMV(mv, vals);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Positions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Textures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// pull up normal coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Normals")]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, normal);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		phongShader.setMaterial(currentMat);

		gl.glDrawArrays(GL_TRIANGLES, 0, obj.getNumVertices());
	}

	private void addToShadow(GL4 gl, String name, WorldObject obj) {
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		pass1Shader.setup(mvStack, vals);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Positions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

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
				702f);
		glassMat = new Material(new float[] { 1f, 1f, 1f, 1.0f }, new float[] { 1f, 1f, 1f, 1.0f },
				new float[] { 1f, 1f, 1f, 1.0f }, 702f);

		pewterMat = new Material(new float[] { .11f, .06f, .11f, 1.0f }, new float[] { .43f, .47f, .54f, 1.0f },
				new float[] { .33f, .33f, .52f, 1.0f }, 79.85f);

		paperMat = new Material(new float[] { .7f, .7f, .7f, 1.0f }, new float[] { 0.8f, 0.8f, 0.8f, 1.0f },
				new float[] { 0.5f, 0.5f, 0.5f, 1.0f }, 50f);
		woodMat = new Material(new float[] { 0.5f, 0.3f, 0.15f, 1.0f }, new float[] { 0.5f, 0.3f, 0.15f, 1.0f },
				new float[] { 0.5f, 0.3f, 0.15f, 1.0f }, 15f);
		leatherMat = new Material(new float[] { .24f, .1f, .07f, 1.0f },
				new float[] { 0.291945f, 0.225797f, 0.221366f, 1.0f }, new float[] { .5f, .5f, .5f, 1.0f }, 60f);
		burlapMat = new Material(new float[] { .24f, .1f, .07f, 1.0f },
				new float[] { 0.291945f, 0.225797f, 0.221366f, 1.0f }, new float[] { .1f, .1f, .1f, 1.0f }, 60f);

		globalAmbientLight = new GlobalAmbientLight();
		mouseLight = new PositionalLight(new float[] { 0.1f, 0.1f, 0.1f, 1.0f }, new float[] { .4f, .3f, .2f, 1.0f },
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, new Vector3f(0f, 0f, 0f));
		resetLight();

		// load assets
		texShader = new FlatShader(gl, "src/a4/texVertShader.glsl", "src/a4/texFragShader.glsl");
		axisShader = new FlatShader(gl, "src/a4/axisVertShader.glsl", "src/a4/axisFragShader.glsl");
		phongShader = new LightingShader(gl, "src/a4/phongVertShader.glsl", "src/a4/phongFragShader.glsl");
		pass1Shader = new BufferShader(gl, "src/a4/vert1Shader.glsl", "src/a4/frag1Shader.glsl");

		woodTex = loadTexture("assets/wood.jpg");
		scrollTex = loadTexture("assets/scroll.png");
		metalTex = loadTexture("assets/metal.jpg");
		yellowTex = loadTexture("assets/coin.png");
		burlapTex = loadTexture("assets/burlap.png");
		leatherTex = loadTexture("assets/leather.png");
		skyboxTex = Utils.loadCubeMap("assets/stars");
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);

		burlapNorm = loadTexture("assets/burlap_normal.jpg");
		woodNorm = loadTexture("assets/wood_normal.jpg");
		metalNorm = loadTexture("assets/metal_normal.jpg");
		leatherNorm = loadTexture("assets/leather_normal.png");
		blankNorm = loadTexture("assets/normal.jpg");

		lightObj = new Sphere();
		scrollObj = new ImportedModel("assets/scroll.obj");
		tableObj = new ImportedModel("assets/table.obj");
		floorObj = new ImportedModel("assets/floor.obj");
		bagObj = new ImportedModel("assets/bag.obj");
		keyObj = new ImportedModel("assets/key.obj");
		coinObj = new ImportedModel("assets/coin_pile.obj");
		bookPagesObj = new ImportedModel("assets/book_pages.obj");
		bookCoverObj = new ImportedModel("assets/book_cover.obj");
		gobletObj = new ImportedModel("assets/goblet.obj");
		gem2Obj = new ImportedModel("assets/gem2.obj");
		b.set(0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.5f, 0.5f, 0.5f, 1.0f);
		setupVertices();
		setupShadowBuffers();
	}

	private void setupShadowBuffers() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		screenSizeX = myCanvas.getWidth();
		screenSizeY = myCanvas.getHeight();
		// create the custom frame buffer
		gl.glGenFramebuffers(1, shadowBuffer, 0);
		// create the shadow texture and configure it to hold depth information.
		// these steps are similar to those in Program 5.2
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, screenSizeX, screenSizeY, 0, GL_DEPTH_COMPONENT,
				GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		float[] axisPositions = { 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f, -1.0f };

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		addToVbo(gl, tableObj, "table");
		addToVbo(gl, floorObj, "floor");
		addToVbo(gl, scrollObj, "scroll");
		addToVbo(gl, keyObj, "key");
		addToVbo(gl, bagObj, "bag");
		addToVbo(gl, coinObj, "coin");
		addToVbo(gl, gobletObj, "goblet");
		addToVbo(gl, gem2Obj, "gem2");
		addToVbo(gl, bookPagesObj, "bookPages");
		addToVbo(gl, bookCoverObj, "bookCover");

		// light - sphere
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

		// skybox

		float[] cubeVertexPositions = { -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
				1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
				-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
				-1.0f };

		float[] cubeTextureCoord = { 1.00f, 0.6666666f, 1.00f, 0.3333333f, 0.75f, 0.3333333f, // back face lower right
				0.75f, 0.3333333f, 0.75f, 0.6666666f, 1.00f, 0.6666666f, // back face upper left
				0.75f, 0.3333333f, 0.50f, 0.3333333f, 0.75f, 0.6666666f, // right face lower right
				0.50f, 0.3333333f, 0.50f, 0.6666666f, 0.75f, 0.6666666f, // right face upper left
				0.50f, 0.3333333f, 0.25f, 0.3333333f, 0.50f, 0.6666666f, // front face lower right
				0.25f, 0.3333333f, 0.25f, 0.6666666f, 0.50f, 0.6666666f, // front face upper left
				0.25f, 0.3333333f, 0.00f, 0.3333333f, 0.25f, 0.6666666f, // left face lower right
				0.00f, 0.3333333f, 0.00f, 0.6666666f, 0.25f, 0.6666666f, // left face upper left
				0.25f, 0.3333333f, 0.50f, 0.3333333f, 0.50f, 0.0000000f, // bottom face upper right
				0.50f, 0.0000000f, 0.25f, 0.0000000f, 0.25f, 0.3333333f, // bottom face lower left
				0.25f, 1.0000000f, 0.50f, 1.0000000f, 0.50f, 0.6666666f, // top face upper right
				0.50f, 0.6666666f, 0.25f, 0.6666666f, 0.25f, 1.0000000f // top face lower left
		};

		vboDict.put("skyboxPositions", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("skyboxPositions")]);
		FloatBuffer skyboxBufShuttle = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxBufShuttle.limit() * 4, skyboxBufShuttle, GL_STATIC_DRAW);

		vboDict.put("skyboxTextures", vboDict.size());
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("skyboxTextures")]);
		FloatBuffer skyboxTexBufShuttle = Buffers.newDirectFloatBuffer(cubeTextureCoord);
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxTexBufShuttle.limit() * 4, skyboxTexBufShuttle, GL_STATIC_DRAW);

		// axis
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
		new Test();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void dispose(GLAutoDrawable drawable) {
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