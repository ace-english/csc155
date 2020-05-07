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
import javax.swing.Spring;

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
	Random random = new Random();
	private Shader texShader, axisShader, phongShader, pass1Shader, chromeShader, glassShader, skyboxShader;
	private int vao[] = new int[1];
	private int vbo[] = new int[50];
	private Camera camera;
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private Matrix4f invTr = new Matrix4f();
	private Matrix4f mv = new Matrix4f();
	private int sLoc, mvLocTex, projLocTex, mvLocSky, projLocSky, mvLocAxis, projLocAxis, mvLocPhong, projLocPhong,
			nLocPhong, mvLocGlass, projLocGlass, nLocGlass, mvLocChrome, projLocChrome, nLocChrome;
	private float aspect;
	private double tf;
	private boolean showAxes, showLight;
	private int[] mouseDragCurrent;

	private ImportedModel tableObj, scrollObj, bagObj, keyObj, coinObj, bookPagesObj, bookCoverObj, gobletObj, gem2Obj,
			floorObj;
	private Sphere lightObj;
	private int woodTex, scrollTex, burlapTex, metalTex, leatherTex, yellowTex, skyboxTex;
	private int woodNorm, blankNorm, burlapNorm, metalNorm, leatherNorm;
	private Material goldMat, leatherMat, woodMat, burlapMat, paperMat, pewterMat;
	private Light globalAmbientLight;
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

	public Starter() {
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

		pass1Shader.use();
		sLoc = gl.glGetUniformLocation(pass1Shader.getShader(), "shadowMVP");
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

		texShader.use();
		texShader.updateLocation("mv_matrix", mvStack, vals);
		texShader.updateLocation("proj_matrix", pMat, vals);
		// gl.glUniformMatrix4fv(mvLocTex, 1, false, mv.get(vals));
		// gl.glUniformMatrix4fv(projLocTex, 1, false, pMat.get(vals));

		chromeShader.use();
chromeShader.updateLocation("mv_matrix",mvStack,vals);
chromeShader.updateLocation("proj_matrix",pMat,vals);
chromeShader.updateLocation("norm_matrix",invTr,vals);

		phongShader.use();
		mvLocPhong = gl.glGetUniformLocation(phongShader.getShader(), "mv_matrix");
		projLocPhong = gl.glGetUniformLocation(phongShader.getShader(), "proj_matrix");
		nLocPhong = gl.glGetUniformLocation(phongShader.getShader(), "norm_matrix");
		sLoc = gl.glGetUniformLocation(phongShader.getShader(), "shadowMVP");
		gl.glUniformMatrix4fv(mvLocPhong, 1, false, mv.get(vals));
		gl.glUniformMatrix4fv(projLocPhong, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLocPhong, 1, false, invTr.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		glassShader.use();
		mvLocGlass = gl.glGetUniformLocation(glassShader.getShader(), "mv_matrix");
		projLocGlass = gl.glGetUniformLocation(glassShader.getShader(), "proj_matrix");
		nLocGlass = gl.glGetUniformLocation(glassShader.getShader(), "norm_matrix");
		gl.glUniformMatrix4fv(mvLocGlass, 1, false, mv.get(vals));
		gl.glUniformMatrix4fv(projLocGlass, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLocGlass, 1, false, invTr.get(vals));

		/**
		 * some sort of buffer flimflam
		 */

		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		// render reflection scene to reflection buffer ----------------

		gl.glBindFramebuffer(GL_FRAMEBUFFER, reflectFrameBuffer);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		renderSkyBoxPrep();
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW); // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		// render refraction scene to refraction buffer
		// ----------------------------------------

		gl.glBindFramebuffer(GL_FRAMEBUFFER, refractFrameBuffer);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);

		renderSkyBoxPrep();
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW); // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		// renderFloorPrep();

		phongShader.use();
		if (showLight) {
			phongShader.installLights(mv, globalAmbientLight, mouseLight);
		} else {
			phongShader.uninstallLights(mv, globalAmbientLight, mouseLight);
		}

		gl.glUniformMatrix4fv(sLoc, 1, false, mvStack.get(vals));
		gl.glUniformMatrix4fv(mvLocPhong, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("tablePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("tableTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// pull up normal coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("tableNormals")]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, woodTex);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);

		/**
		 * end flimflam
		 */

		// ---------------------- skybox
		renderSkyBoxPrep();

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW); // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		mvStack.translate(new Vector3f(camera.getLocation()).negate());

		// ---------------------- axis
		if (showAxes) {

			axisShader.use();
			mvLocAxis = gl.glGetUniformLocation(axisShader.getShader(), "mv_matrix");
			projLocAxis = gl.glGetUniformLocation(axisShader.getShader(), "proj_matrix");
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

		// light
		if (showLight) {
			texShader.use();
			mvStack.pushMatrix();
			mvStack.translate(mouseLight.getPosition());
			mvStack.scale(.05f, .05f, .05f);
			gl.glUniformMatrix4fv(mvLocTex, 1, false, mvStack.get(vals));
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightPositions")]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			// pull up texture coords
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("lightTextures")]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2); // activate texture object
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, yellowTex);
			gl.glEnable(GL_DEPTH_TEST);

			gl.glDrawArrays(GL_TRIANGLES, 0, lightObj.getNumVertices());
			mvStack.popMatrix();
			phongShader.installLights(mv, globalAmbientLight, mouseLight);
			glassShader.installLights(mv, globalAmbientLight, mouseLight);
			chromeShader.installLights(mv, globalAmbientLight, mouseLight);
		} else {
			phongShader.uninstallLights(mv, globalAmbientLight, mouseLight);
			glassShader.uninstallLights(mv, globalAmbientLight, mouseLight);
			chromeShader.uninstallLights(mv, globalAmbientLight, mouseLight);
		}

		phongShader.use();

		// addToDisplay("floor", woodTex, woodNorm, woodMat, floorObj, phongShader);
		addToDisplay("table", woodTex, woodNorm, woodMat, tableObj, phongShader);
		addToDisplay("scroll", scrollTex, blankNorm, paperMat, scrollObj, phongShader);
		mvStack.pushMatrix();
		mvStack.translate(new Vector3f(0f, 0f, 2f));
		phongShader.updateLocation("mv_stack", mvStack, vals);
		addToDisplay("bag", burlapTex, burlapNorm, burlapMat, bagObj, phongShader);
		addToDisplayChrome("coin", goldMat, coinObj);
		mvStack.popMatrix();

		addToDisplay("bookCover", leatherTex, leatherNorm, leatherMat, bookCoverObj, phongShader);
		addToDisplay("bookPages", scrollTex, blankNorm, paperMat, bookPagesObj, phongShader);

		// ---------------------chrome KEY

		addToDisplayChrome("key", pewterMat, keyObj);
		addToDisplayChrome("goblet", goldMat, gobletObj);

		// ------------------------------------------------- gems
		glassShader.use();
		String name = "gem2";
		WorldObject obj = gem2Obj;
		gl.glUniformMatrix4fv(mvLocPhong, 1, false, mvStack.get(vals));
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
		gl.glBindTexture(GL_TEXTURE_2D, reflectTextureId);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, refractTextureId);

		// gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		glassShader.setMaterial(goldMat);

		gl.glDrawArrays(GL_TRIANGLES, 0, obj.getNumVertices());

		mvStack.popMatrix(); // final pop

	}

	public void addToDisplayChrome(String name, Material currentMat, WorldObject obj) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		chromeShader.use();

		chromeShader.updateLocation("mv_matrix", mvStack, vals);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Positions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up normal coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get(name + "Normals")]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTex);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		chromeShader.setMaterial(currentMat);

		gl.glDrawArrays(GL_TRIANGLES, 0, obj.getNumVertices());

	}

	void renderSkyBoxPrep() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		skyboxShader.use();

		skyboxShader.updateLocation("v_matrix", mv, vals);
		skyboxShader.updateLocation("proj_matrix", pMat, vals);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("skyboxPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTex);
	}

	private void addToDisplay(String name, int texture0, int texture1, Material currentMat, WorldObject obj,
			Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.updateLocation("mv_matrix", mvStack, vals);
		shader.updateLocation("shadowMVP", mvStack, vals);
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
		gl.glBindTexture(GL_TEXTURE_2D, texture0);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, texture1);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		shader.setMaterial(currentMat);

		gl.glDrawArrays(GL_TRIANGLES, 0, obj.getNumVertices());
	}

	private void addToShadow(GL4 gl, String name, WorldObject obj) {
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		sLoc = gl.glGetUniformLocation(pass1Shader.getShader(), "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, mvStack.get(vals));
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
		camera = new Camera(new Vector3f(0f, 1f, 2f));
		showAxes = false;
		showLight = true;

		goldMat = new Material(Utils.goldAmbient(), Utils.goldDiffuse(), Utils.goldSpecular(), Utils.goldShininess());

		pewterMat = new Material(new float[] { .11f, .06f, .11f, 1.0f }, new float[] { .43f, .47f, .54f, 1.0f },
				new float[] { .13f, .13f, .22f, 1.0f }, 79.85f);

		paperMat = new Material(new float[] { .7f, .7f, .7f, 1.0f }, new float[] { 0.8f, 0.8f, 0.8f, 1.0f },
				new float[] { 0.5f, 0.5f, 0.5f, 1.0f }, 50f);
		woodMat = new Material(new float[] { 0.5f, 0.3f, 0.15f, 1.0f }, new float[] { 0.5f, 0.3f, 0.15f, 1.0f },
				new float[] { 0.5f, 0.3f, 0.15f, 1.0f }, 15f);
		leatherMat = new Material(new float[] { .5f, .2f, .1f, 1.0f }, new float[] { 0.5f, 0.35f, 0.35f, 1.0f },
				new float[] { .5f, .5f, .5f, 1.0f }, 60f);
		burlapMat = new Material(new float[] { .24f, .1f, .07f, 1.0f },
				new float[] { 0.291945f, 0.225797f, 0.221366f, 1.0f }, new float[] { .1f, .1f, .1f, 1.0f }, 60f);

		generateNoise();
		noiseTexture = buildNoiseTexture();

		globalAmbientLight = new GlobalAmbientLight(new float[] { .7f, .7f, .9f });
		mouseLight = new PositionalLight(new float[] { 0.1f, 0.1f, 0.1f, 1.0f }, new float[] { .4f, .3f, .2f, 1.0f },
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, new Vector3f(0f, 0f, 0f));
		resetLight();

		// load assets
		texShader = new Shader(gl, "src/a4/texVertShader.glsl", "src/a4/texFragShader.glsl");
		axisShader = new Shader(gl, "src/a4/axisVertShader.glsl", "src/a4/axisFragShader.glsl");
		phongShader = new Shader(gl, "src/a4/phongVertShader.glsl", "src/a4/phongFragShader.glsl");
		glassShader = new Shader(gl, "src/a4/glassVertShader.glsl", "src/a4/glassFragShader.glsl");
		pass1Shader = new Shader(gl, "src/a4/vert1Shader.glsl", "src/a4/frag1Shader.glsl");
		chromeShader = new Shader(gl, "src/a4/chromeVertShader.glsl", "src/a4/chromeFragShader.glsl");
		skyboxShader = new Shader(gl, "src/a4/cubeVertShader.glsl", "src/a4/cubeFragShader.glsl");

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

	private void fillDataArray(byte data[]) {
		double xyPeriod = 30.0;
		double turbPower = 0.15;
		double turbSize = 40.0;

		for (int i = 0; i < noiseWidth; i++) {
			for (int j = 0; j < noiseHeight; j++) {
				for (int k = 0; k < noiseDepth; k++) {
					double xValue = (i - (double) noiseWidth / 2.0) / (double) noiseWidth;
					double yValue = (j - (double) noiseHeight / 2.0) / (double) noiseHeight;
					double distValue = Math.sqrt(xValue * xValue + yValue * yValue)
							+ turbPower * turbulence(i, j, k, turbSize) / 256.0;
					double sineValue = 128.0 * Math.abs(Math.sin(2.0 * xyPeriod * distValue * Math.PI));

					Color c = new Color((int) (60 + (int) sineValue), (int) (10 + (int) sineValue), 0);

					data[i * (noiseWidth * noiseHeight * 4) + j * (noiseHeight * 4) + k * 4 + 0] = (byte) c.getRed();
					data[i * (noiseWidth * noiseHeight * 4) + j * (noiseHeight * 4) + k * 4 + 1] = (byte) c.getGreen();
					data[i * (noiseWidth * noiseHeight * 4) + j * (noiseHeight * 4) + k * 4 + 2] = (byte) c.getBlue();
					data[i * (noiseWidth * noiseHeight * 4) + j * (noiseHeight * 4) + k * 4 + 3] = (byte) 255;
				}
			}
		}
	}

	private int buildNoiseTexture() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseHeight * noiseWidth * noiseDepth * 4];

		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, noiseWidth, noiseHeight, noiseDepth, GL_RGBA,
				GL_UNSIGNED_INT_8_8_8_8_REV, bb);

		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generateNoise() {
		for (int x = 0; x < noiseHeight; x++) {
			for (int y = 0; y < noiseWidth; y++) {
				for (int z = 0; z < noiseDepth; z++) {
					noise[x][y][z] = random.nextDouble();
				}
			}
		}
	}

	double smoothNoise(double x1, double y1, double z1) { // get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		// neighbor values
		int x2 = ((int) x1 + noiseWidth + 1) % noiseWidth;
		int y2 = ((int) y1 + noiseHeight + 1) % noiseHeight;
		int z2 = ((int) z1 + noiseDepth + 1) % noiseDepth;

		// smooth the noise by interpolating
		double value = 0.0;
		value += (1 - fractX) * (1 - fractY) * (1 - fractZ) * noise[(int) x1][(int) y1][(int) z1];
		value += (1 - fractX) * fractY * (1 - fractZ) * noise[(int) x1][(int) y2][(int) z1];
		value += fractX * (1 - fractY) * (1 - fractZ) * noise[(int) x2][(int) y1][(int) z1];
		value += fractX * fractY * (1 - fractZ) * noise[(int) x2][(int) y2][(int) z1];

		value += (1 - fractX) * (1 - fractY) * fractZ * noise[(int) x1][(int) y1][(int) z2];
		value += (1 - fractX) * fractY * fractZ * noise[(int) x1][(int) y2][(int) z2];
		value += fractX * (1 - fractY) * fractZ * noise[(int) x2][(int) y1][(int) z2];
		value += fractX * fractY * fractZ * noise[(int) x2][(int) y2][(int) z2];

		return value;
	}

	private double turbulence(double x, double y, double z, double size) {
		double value = 0.0, initialSize = size;
		while (size >= 0.9) {
			value = value + smoothNoise(x / size, y / size, z / size) * size;
			size = size / 2.0;
		}
		value = 128.0 * value / initialSize;
		return value;
	}

	public static void main(String[] args) {
		new Starter();
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
