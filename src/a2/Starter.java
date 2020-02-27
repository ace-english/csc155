package a2;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_CONTROL_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TESS_EVALUATION_SHADER;

import java.io.*;
import java.nio.*;
import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.*;

public class Starter extends JFrame implements GLEventListener {
	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[7];
	private float cameraX, cameraY, cameraZ;
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float pyrLocX, pyrLocY, pyrLocZ;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, projLoc;
	private float aspect;
	private double tf;
	private int numSphereVerts;
	private ImportedModel mugObj;
	private ImportedModel coinObj;
	private ImportedModel rocketObj;
	private int neptuneTex;
	private Dictionary<String, Integer> vboDict;

	public Starter() {
		setTitle("Assignment 2");
		setSize(1000, 600);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		int rainbowLoc = gl.glGetUniformLocation(renderingProgram, "rainbow"); // retrieve pointer to "rainbow"

		// push view matrix onto the stack
		mvStack.pushMatrix();
		mvStack.translate(-cameraX, -cameraY, -cameraZ);

		tf = elapsedTime / 1000.0; // time factor


		//use texture shader
		gl.glProgramUniform1f(renderingProgram, rainbowLoc, 0);
		// ---------------------- sun
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		//mvStack.rotate((float) tf, 1.0f, 0.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("spherePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix();	//print sun

		// ----------------------- cube == planet
		mvStack.pushMatrix();
		mvStack.translate((float) Math.sin(tf) * 4.0f, 0.0f, (float) Math.cos(tf) * 4.0f);
		mvStack.pushMatrix();
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("cubePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix();	//print planet 1


		//use rainbow shader
		gl.glProgramUniform1f(renderingProgram, rainbowLoc, 1);
		// ----------------------- gem == moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float) Math.sin(tf) * 2.0f, (float) Math.cos(tf) * 2.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("gemPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 287);
		mvStack.popMatrix();	//print moon 1
		


		// ----------------------- second gem moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float) Math.sin(tf) * -2.0f, (float) Math.cos(tf) * -2.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("gemPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 287);
		mvStack.popMatrix();	//print moon 2
		mvStack.popMatrix();	//pop moon orbital
		mvStack.popMatrix();	//pop planet orbital
/*
		// ----------------------- second planet - mug
		mvStack.pushMatrix();
		mvStack.translate((float) Math.sin(tf) * -7.0f, 0.0f, (float) Math.cos(tf) * -7.0f);
		mvStack.pushMatrix();
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, mugObj.getNumVertices());
		mvStack.popMatrix();	//print planet 1
		
		// ----------------------- moon - shuttle
		
		// ----------------------- Satellite - coin
		
		
		mvStack.popMatrix();	//leave planet orbital
		*/
		
		
		mvStack.popMatrix();	//final pop
		
	}

	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = createShaderProgram("src/a2/vertShader.glsl", "src/a2/fragShader.glsl");
		neptuneTex = loadTexture("assets/neptune.jpg");
		vboDict=new Hashtable<String, Integer>();
		//mugObj=new ImportedModel("assets/coin.obj");
		//coinObj=new ImportedModel("assets/coin.obj");
		//rocketObj=new ImportedModel("assets/shuttle.obj");
		setupVertices();
		
		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 12.0f;
	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] cubePositions = { 
				//back face
				-1.0f, 1.0f, -1.0f, //upper right
				-1.0f, -1.0f, -1.0f, //bottom right
				1.0f, -1.0f, -1.0f, //bottom left
				
				1.0f, -1.0f, -1.0f, //bottom left
				1.0f, 1.0f, -1.0f,  //upper left
				-1.0f, 1.0f, -1.0f,  //upper right
				
				//right face
				1.0f, -1.0f, -1.0f, //bottom right
				1.0f, -1.0f, 1.0f,  //bottom left
				1.0f, 1.0f, -1.0f, //upper right
				
				1.0f, -1.0f, 1.0f, //bottom left
				1.0f, 1.0f, 1.0f, //upper left
				1.0f, 1.0f, -1.0f, //upper right
				
				//front face
				1.0f, -1.0f, 1.0f, //lower right
				-1.0f, -1.0f, 1.0f, //lower left
				1.0f, 1.0f, 1.0f, //upper right
				
				-1.0f, -1.0f, 1.0f,  //lower left
				-1.0f, 1.0f, 1.0f, //upper left
				1.0f, 1.0f, 1.0f,  //upper right
				
				//left face
				-1.0f, -1.0f, 1.0f, //lower right
				-1.0f, -1.0f, -1.0f, //lower left
				-1.0f,	1.0f, 1.0f,  //upper right
				
				-1.0f, -1.0f, -1.0f,  //lower left
				-1.0f, 1.0f, -1.0f,  //upper left
				-1.0f, 1.0f, 1.0f,  //upper right
				
				//bottom face
				-1.0f, -1.0f, 1.0f, //upper left
				1.0f, -1.0f, 1.0f, //upper right
				1.0f, -1.0f, -1.0f, //lower left
				
				1.0f, -1.0f, -1.0f,  //lower left
				-1.0f, -1.0f, -1.0f, //lower right
				-1.0f, -1.0f, 1.0f,  //upper left
				
				//top face
				-1.0f, 1.0f, -1.0f,   //upper left
				1.0f, 1.0f, -1.0f, //upper right
				1.0f, 1.0f, 1.0f, //lower right
				
				1.0f, 1.0f, 1.0f,  //lower right
				-1.0f, 1.0f, 1.0f, //lower left
				-1.0f, 1.0f, -1.0f //upper left
			}; 

		float[] cubeTextureCoordinates= {
				//back face
				1.0f, 1.0f, //upper right
				1.0f, 0.0f, //bottom right
				0.0f, 0.0f, //bottom left
				
				0.0f, 0.0f, //bottom left
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right

				//right face
				1.0f, 0.0f, //bottom right
				0.0f, 0.0f, //bottom left
				1.0f, 1.0f, //upper right

				0.0f, 0.0f, //bottom left
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right

				//front face
				1.0f, 0.0f, //bottom right
				0.0f, 0.0f, //bottom left
				1.0f, 1.0f, //upper right

				0.0f, 0.0f, //bottom left
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right
				
				//left face
				1.0f, 0.0f, //bottom right
				0.0f, 0.0f, //bottom left
				1.0f, 1.0f, //upper right

				0.0f, 0.0f, //bottom left
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right
				
				//bottom face
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right
				0.0f, 0.0f, //bottom left

				0.0f, 0.0f, //bottom left
				1.0f, 0.0f, //bottom right
				0.0f, 1.0f, //upper left
				
				//top face
				0.0f, 1.0f, //upper left
				1.0f, 1.0f, //upper right
				0.0f, 0.0f, //bottom left
				
				0.0f, 0.0f, //bottom left
				1.0f, 0.0f, //bottom right
				0.0f, 1.0f, //upper left

				
				
		};
		
		
		float[] pyramidPositions = { -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // left
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, // LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f // RR
		};

		float[] pyrTextureCoordinates =
			{	0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
			};
		
		float[] gemPositions = {
				// top
				1, 1, 3, 0, 1.3f, 0, -1, 1, 3, -1, 1, 3, 0, 1.3f, 0, -3, 1, 1, -3, 1, 1, 0, 1.3f, 0, -3, 1, -1, -3, 1,
				-1, 0, 1.3f, 0, -1, 1, -3, -1, 1, -3, 0, 1.3f, 0, 1, 1, -3, 1, 1, -3, 0, 1.3f, 0, 3, 1, -1, 3, 1, -1, 0,
				1.3f, 0, 3, 1, 1, 3, 1, 1, 0, 1.3f, 0, 1, 1, 3,
				// sides
				1, 1, 3, -1, 1, 3, 0, 0, 4, 0, 0, 4, -1, 1, 3, -3, 0, 3, -3, 0, 3, -1, 1, 3, -3, 1, 1, -3, 1, 1, -4, 0,
				0, -3, 0, 3, -4, 0, 0, -3, 1, 1, -3, 1, -1, -3, 1, -1, -3, 0, -3, -4, 0, 0, -3, 1, -1, -1, 1, -3, -3, 0,
				-3, -3, 0, -3, -1, 1, -3, 0, 0, -4, -1, 1, -3, 1, 1, -3, 0, 0, -4, 3, 0, -3, 0, 0, -4, 1, 1, -3, 1, 1,
				-3, 3, 1, -1, 3, 0, -3, 3, 0, -3, 3, 1, -1, 4, 0, 0, 3, 1, -1, 3, 1, 1, 4, 0, 0, 4, 0, 0, 3, 1, 1, 3, 0,
				3, 3, 1, 1, 1, 1, 3, 3, 0, 3, 0, 0, 4, 3, 0, 3, 1, 1, 3,
				// bottom
				3, 0, -3, 4, 0, 0, 0, -4, 0, 0, 0, -4, 3, 0, -3, 0, -4, 0, -3, 0, -3, 0, 0, -4, 0, -4, 0, -3, 0, -3, 0,
				-4, 0, -4, 0, 0, -4, 0, 0, 0, -4, 0, -3, 0, 3, -3, 0, 3, 0, -4, 0, 0, 0, 4, 3, 0, 3, 0, 0, 4, 0, -4, 0,
				3, 0, 3, 0, -4, 0, 4, 0, 0

		};
		Sphere mySphere = new Sphere(96);
		numSphereVerts = mySphere.getIndices().length;
	
		int[] indicesSphere = mySphere.getIndices();
		
		float[] pvaluesSphere = new float[indicesSphere.length*3];
		float[] tvaluesSphere = new float[indicesSphere.length*2];
		float[] nvaluesSphere = new float[indicesSphere.length*3];
		
		for (int i=0; i<indicesSphere.length; i++)
		{	pvaluesSphere[i*3] = (float) (mySphere.getVertices()[indicesSphere[i]]).x;
			pvaluesSphere[i*3+1] = (float) (mySphere.getVertices()[indicesSphere[i]]).y;
			pvaluesSphere[i*3+2] = (float) (mySphere.getVertices()[indicesSphere[i]]).z;
			tvaluesSphere[i*2] = (float) (mySphere.getTexCoords()[indicesSphere[i]]).x;
			tvaluesSphere[i*2+1] = (float) (mySphere.getTexCoords()[indicesSphere[i]]).y;
			nvaluesSphere[i*3] = (float) (mySphere.getNormals()[indicesSphere[i]]).x;
			nvaluesSphere[i*3+1]= (float)(mySphere.getNormals()[indicesSphere[i]]).y;
			nvaluesSphere[i*3+2]=(float) (mySphere.getNormals()[indicesSphere[i]]).z;
		}
		
		
		/*
		float[] pvaluesMug = new float[mugObj.getNumVertices()*3];
		float[] tvaluesMug = new float[mugObj.getNumVertices()*2];
		float[] nvaluesMug = new float[mugObj.getNumVertices()*3];
		
		for (int i=0; i<mugObj.getNumVertices(); i++)
		{	pvaluesMug[i*3]   = (float) (mugObj.getVertices()[i]).x();
			pvaluesMug[i*3+1] = (float) (mugObj.getVertices()[i]).y();
			pvaluesMug[i*3+2] = (float) (mugObj.getVertices()[i]).z();
			tvaluesMug[i*2]   = (float) (mugObj.getTexCoords()[i]).x();
			tvaluesMug[i*2+1] = (float) (mugObj.getTexCoords()[i]).y();
			nvaluesMug[i*3]   = (float) (mugObj.getNormals()[i]).x();
			nvaluesMug[i*3+1] = (float) (mugObj.getNormals()[i]).y();
			nvaluesMug[i*3+2] = (float) (mugObj.getNormals()[i]).z();
		}
		
		
*/
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		vboDict.put("cubePositions", 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("cubePositions")]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);

		vboDict.put("cubeTextures", 1);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("cubeTextures")]);
		FloatBuffer cubeTexBuf = Buffers.newDirectFloatBuffer(cubeTextureCoordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeTexBuf.limit() * 4, cubeTexBuf, GL_STATIC_DRAW);
		
		vboDict.put("pyramidPositions", 2);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("pyramidPositions")]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf, GL_STATIC_DRAW);
		
		vboDict.put("pyramidTextures", 3);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("pyramidTextures")]);
		FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyrTextureCoordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrTexBuf.limit() * 4, pyrTexBuf, GL_STATIC_DRAW);

		vboDict.put("gemPositions", 4);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("gemPositions")]);
		FloatBuffer gemBuf = Buffers.newDirectFloatBuffer(gemPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, gemBuf.limit() * 4, gemBuf, GL_STATIC_DRAW);

		vboDict.put("spherePositions", 5);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("spherePositions")]);
		FloatBuffer vertSphereBuf = Buffers.newDirectFloatBuffer(pvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, vertSphereBuf.limit()*4, vertSphereBuf, GL_STATIC_DRAW);

		vboDict.put("sphereTextures", 6);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("sphereTextures")]);
		FloatBuffer texSphereBuf = Buffers.newDirectFloatBuffer(tvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, texSphereBuf.limit()*4, texSphereBuf, GL_STATIC_DRAW);
		
		/*
		vboDict.put("mugPositions",7);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vbo.get("mugPositions")]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvaluesMug);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		vboDict.put("mugTextures",8);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vbo.get("mugTextures")]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvaluesMug);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW); */
		System.out.println("initialized vbo");
		System.out.println(vboDict);
		
	}

	public static void main(String[] args) {
		new Starter();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void dispose(GLAutoDrawable drawable) {
	}

	private static int createShaderProgram(String vS, String tCS, String tES, String gS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader = prepareShader(GL_VERTEX_SHADER, vS);
		int tcShader = prepareShader(GL_TESS_CONTROL_SHADER, tCS);
		int teShader = prepareShader(GL_TESS_EVALUATION_SHADER, tES);
		int gShader = prepareShader(GL_GEOMETRY_SHADER, gS);
		int fShader = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vtgfprogram = gl.glCreateProgram();
		gl.glAttachShader(vtgfprogram, vShader);
		gl.glAttachShader(vtgfprogram, tcShader);
		gl.glAttachShader(vtgfprogram, teShader);
		gl.glAttachShader(vtgfprogram, gShader);
		gl.glAttachShader(vtgfprogram, fShader);
		finalizeProgram(vtgfprogram);
		return vtgfprogram;
	}

	private static int createShaderProgram(String vS, String tCS, String tES, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader = prepareShader(GL_VERTEX_SHADER, vS);
		int tcShader = prepareShader(GL_TESS_CONTROL_SHADER, tCS);
		int teShader = prepareShader(GL_TESS_EVALUATION_SHADER, tES);
		int fShader = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vtfprogram = gl.glCreateProgram();
		gl.glAttachShader(vtfprogram, vShader);
		gl.glAttachShader(vtfprogram, tcShader);
		gl.glAttachShader(vtfprogram, teShader);
		gl.glAttachShader(vtfprogram, fShader);
		finalizeProgram(vtfprogram);
		return vtfprogram;
	}

	private static int createShaderProgram(String vS, String gS, String fS) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int vShader = prepareShader(GL_VERTEX_SHADER, vS);
		int gShader = prepareShader(GL_GEOMETRY_SHADER, gS);
		int fShader = prepareShader(GL_FRAGMENT_SHADER, fS);
		int vgfprogram = gl.glCreateProgram();
		gl.glAttachShader(vgfprogram, vShader);
		gl.glAttachShader(vgfprogram, gShader);
		gl.glAttachShader(vgfprogram, fShader);
		finalizeProgram(vgfprogram);
		return vgfprogram;
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
	
	private static int loadTexture(String textureFileName)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int finalTextureRef;
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		finalTextureRef = tex.getTextureObject();

		// building a mipmap and use anisotropic filtering
		gl.glBindTexture(GL_TEXTURE_2D, finalTextureRef);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		return finalTextureRef;
	}
}