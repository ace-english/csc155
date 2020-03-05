package a2;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
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

public class Starter extends JFrame implements GLEventListener {
	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[13];
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
	private ImportedModel shuttleObj;
	private int neptuneTex;
	private int mugTex;
	private int brickTex;
	private int coinTex;
	private int shuttleTex;
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

		// use texture shader
		gl.glProgramUniform1f(renderingProgram, rainbowLoc, 0);
		// ---------------------- sun
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		// mvStack.rotate((float) tf, 1.0f, 0.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("spherePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("sphereTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, neptuneTex);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix(); // print sun

		// ----------------------- cube == planet
		mvStack.pushMatrix();
		mvStack.translate((float) Math.sin(tf) * 4.0f, 0.0f, (float) Math.cos(tf) * 4.0f);
		mvStack.pushMatrix();
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("cubePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("cubeTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, brickTex);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix(); // print planet 1

		// use rainbow shader
		gl.glProgramUniform1f(renderingProgram, rainbowLoc, 1);
		// ----------------------- gem == moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float) Math.sin(tf) * 2.0f, (float) Math.cos(tf) * 2.0f);
		mvStack.scale(0.1f, 0.1f, 0.1f);
		mvStack.rotateXYZ(0, 1.5f * (float) tf, 3f * (float) tf);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("gemPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 287);
		mvStack.popMatrix(); // print moon 1

		// ----------------------- second gem moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float) Math.sin(tf) * -2.0f, (float) Math.cos(tf) * -2.0f);
		mvStack.scale(0.1f, 0.1f, 0.1f);
		mvStack.rotateXYZ(0, -1.5f * (float) tf, 3f * (float) tf);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("gemPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 287);
		mvStack.popMatrix(); // print moon 2
		mvStack.popMatrix(); // pop moon orbital
		mvStack.popMatrix(); // pop planet orbital

		// use texture shader
		gl.glProgramUniform1f(renderingProgram, rainbowLoc, 0);
		
		  // ----------------------- second planet - mug 
		mvStack.pushMatrix();
		mvStack.translate((float) Math.sin(tf) * -7.0f, 0.0f, (float) Math.cos(tf) *-7.0f); 
		//mvStack.scale(10f, 10f, 10f);
		//mvStack.rotateXYZ(0, -.5f * (float) tf, 1f * (float) tf);
		mvStack.pushMatrix(); 
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals)); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("mugPositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0); 
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("mugTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, mugTex);
		gl.glDrawArrays(GL_TRIANGLES, 0,mugObj.getNumVertices());
		mvStack.popMatrix(); //print planet 2
	
		  
		  // ----------------------- moon - shuttle
		mvStack.pushMatrix();
		mvStack.translate((float) Math.sin(tf) * 1.0f, (float) Math.cos(tf) * 1.0f, (float) Math.cos(tf) * 1.0f); 
		mvStack.rotateXYZ(0f,0f,0f);
		mvStack.pushMatrix(); 
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals)); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("shuttlePositions")]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0); 
		// pull up texture coords
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("shuttleTextures")]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		// activate texture object
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shuttleTex);
		gl.glDrawArrays(GL_TRIANGLES, 0,shuttleObj.getNumVertices());
		mvStack.popMatrix(); //print shuttle
		  
		  // ----------------------- Satellite - coin
		  
		  
		  mvStack.popMatrix(); //leave planet orbital
		  mvStack.popMatrix(); //leave planet orbital
		 

		mvStack.popMatrix(); // final pop

	}

	public void init(GLAutoDrawable drawable) {
		startTime = System.currentTimeMillis();
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		vboDict = new Hashtable<String, Integer>();
		//renderingProgram = Utils.createShaderProgram("src/notes6/vert.shader", "src/notes6/frag.shader");
		renderingProgram = createShaderProgram("src/a2/vertShader.glsl", "src/a2/fragShader.glsl");
		neptuneTex = loadTexture("assets/neptune.jpg");
		brickTex = loadTexture("assets/brick1.jpg");
		mugTex = loadTexture("assets/mug.png");
		coinTex = loadTexture("assets/coin.png");
		shuttleTex = loadTexture("assets/shuttle.jpg");
		mugObj = new ImportedModel("assets/mug.obj");
		//coinObj=new ImportedModel("assets/coin.obj");
		shuttleObj=new ImportedModel("assets/shuttle.obj");
		setupVertices();

		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 12.0f;
	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] cubePositions = {
				// back face
				-1.0f, 1.0f, -1.0f, // upper right
				-1.0f, -1.0f, -1.0f, // bottom right
				1.0f, -1.0f, -1.0f, // bottom left

				1.0f, -1.0f, -1.0f, // bottom left
				1.0f, 1.0f, -1.0f, // upper left
				-1.0f, 1.0f, -1.0f, // upper right

				// right face
				1.0f, -1.0f, -1.0f, // bottom right
				1.0f, -1.0f, 1.0f, // bottom left
				1.0f, 1.0f, -1.0f, // upper right

				1.0f, -1.0f, 1.0f, // bottom left
				1.0f, 1.0f, 1.0f, // upper left
				1.0f, 1.0f, -1.0f, // upper right

				// front face
				1.0f, -1.0f, 1.0f, // lower right
				-1.0f, -1.0f, 1.0f, // lower left
				1.0f, 1.0f, 1.0f, // upper right

				-1.0f, -1.0f, 1.0f, // lower left
				-1.0f, 1.0f, 1.0f, // upper left
				1.0f, 1.0f, 1.0f, // upper right

				// left face
				-1.0f, -1.0f, 1.0f, // lower right
				-1.0f, -1.0f, -1.0f, // lower left
				-1.0f, 1.0f, 1.0f, // upper right

				-1.0f, -1.0f, -1.0f, // lower left
				-1.0f, 1.0f, -1.0f, // upper left
				-1.0f, 1.0f, 1.0f, // upper right

				// bottom face
				-1.0f, -1.0f, 1.0f, // upper left
				1.0f, -1.0f, 1.0f, // upper right
				1.0f, -1.0f, -1.0f, // lower left

				1.0f, -1.0f, -1.0f, // lower left
				-1.0f, -1.0f, -1.0f, // lower right
				-1.0f, -1.0f, 1.0f, // upper left

				// top face
				-1.0f, 1.0f, -1.0f, // upper left
				1.0f, 1.0f, -1.0f, // upper right
				1.0f, 1.0f, 1.0f, // lower right

				1.0f, 1.0f, 1.0f, // lower right
				-1.0f, 1.0f, 1.0f, // lower left
				-1.0f, 1.0f, -1.0f // upper left
		};

		float[] cubeTextureCoordinates = {
				// back face
				1.0f, 1.0f, // upper right
				1.0f, 0.0f, // bottom right
				0.0f, 0.0f, // bottom left

				0.0f, 0.0f, // bottom left
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right

				// right face
				1.0f, 0.0f, // bottom right
				0.0f, 0.0f, // bottom left
				1.0f, 1.0f, // upper right

				0.0f, 0.0f, // bottom left
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right

				// front face
				1.0f, 0.0f, // bottom right
				0.0f, 0.0f, // bottom left
				1.0f, 1.0f, // upper right

				0.0f, 0.0f, // bottom left
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right

				// left face
				1.0f, 0.0f, // bottom right
				0.0f, 0.0f, // bottom left
				1.0f, 1.0f, // upper right

				0.0f, 0.0f, // bottom left
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right

				// bottom face
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right
				0.0f, 0.0f, // bottom left

				0.0f, 0.0f, // bottom left
				1.0f, 0.0f, // bottom right
				0.0f, 1.0f, // upper left

				// top face
				0.0f, 1.0f, // upper left
				1.0f, 1.0f, // upper right
				0.0f, 0.0f, // bottom left

				0.0f, 0.0f, // bottom left
				1.0f, 0.0f, // bottom right
				0.0f, 1.0f, // upper left

		};

		float[] pyramidPositions = { -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // front
				1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // right
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // back
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, // left
				-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, // LF
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f // RR
		};

		float[] pyrTextureCoordinates = { 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };

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

		float[] pvaluesSphere = new float[indicesSphere.length * 3];
		float[] tvaluesSphere = new float[indicesSphere.length * 2];
		float[] nvaluesSphere = new float[indicesSphere.length * 3];

		for (int i = 0; i < indicesSphere.length; i++) {
			pvaluesSphere[i * 3] = (float) (mySphere.getVertices()[indicesSphere[i]]).x;
			pvaluesSphere[i * 3 + 1] = (float) (mySphere.getVertices()[indicesSphere[i]]).y;
			pvaluesSphere[i * 3 + 2] = (float) (mySphere.getVertices()[indicesSphere[i]]).z;
			tvaluesSphere[i * 2] = (float) (mySphere.getTexCoords()[indicesSphere[i]]).x;
			tvaluesSphere[i * 2 + 1] = (float) (mySphere.getTexCoords()[indicesSphere[i]]).y;
			nvaluesSphere[i * 3] = (float) (mySphere.getNormals()[indicesSphere[i]]).x;
			nvaluesSphere[i * 3 + 1] = (float) (mySphere.getNormals()[indicesSphere[i]]).y;
			nvaluesSphere[i * 3 + 2] = (float) (mySphere.getNormals()[indicesSphere[i]]).z;
		}

		
		float[] pvaluesMug = new float[mugObj.getNumVertices()*3]; 
		float[] tvaluesMug = new float[mugObj.getNumVertices()*2]; 
		float[] nvaluesMug = new float[mugObj.getNumVertices()*3];
		  
		 for (int i=0; i<mugObj.getNumVertices(); i++) { pvaluesMug[i*3] = (float)
		 (mugObj.getVertices()[i]).x(); pvaluesMug[i*3+1] = (float)
		 (mugObj.getVertices()[i]).y(); pvaluesMug[i*3+2] = (float)
		 (mugObj.getVertices()[i]).z(); tvaluesMug[i*2] = (float)
		 (mugObj.getTexCoords()[i]).x(); tvaluesMug[i*2+1] = (float)
		 (mugObj.getTexCoords()[i]).y(); nvaluesMug[i*3] = (float)
		 (mugObj.getNormals()[i]).x(); nvaluesMug[i*3+1] = (float)
		 (mugObj.getNormals()[i]).y(); nvaluesMug[i*3+2] = (float)
		 (mugObj.getNormals()[i]).z(); }
		 
		 /*
		 float[] pvaluesCoin = new float[coinObj.getNumVertices()*3]; 
			float[] tvaluesCoin = new float[coinObj.getNumVertices()*2]; 
			float[] nvaluesCoin = new float[coinObj.getNumVertices()*3];
			  
			 for (int i=0; i<coinObj.getNumVertices(); i++) { pvaluesMug[i*3] = (float)
			 (coinObj.getVertices()[i]).x(); pvaluesMug[i*3+1] = (float)
			 (coinObj.getVertices()[i]).y(); pvaluesMug[i*3+2] = (float)
			 (coinObj.getVertices()[i]).z(); tvaluesMug[i*2] = (float)
			 (coinObj.getTexCoords()[i]).x(); tvaluesMug[i*2+1] = (float)
			 (coinObj.getTexCoords()[i]).y(); nvaluesMug[i*3] = (float)
			 (coinObj.getNormals()[i]).x(); nvaluesMug[i*3+1] = (float)
			 (coinObj.getNormals()[i]).y(); nvaluesMug[i*3+2] = (float)
			 (coinObj.getNormals()[i]).z(); }
			 
		*/
			 float[] pvaluesShuttle = new float[shuttleObj.getNumVertices()*3]; 
				float[] tvaluesShuttle = new float[shuttleObj.getNumVertices()*2]; 
				float[] nvaluesShuttle = new float[shuttleObj.getNumVertices()*3];
				  
				 for (int i=0; i<shuttleObj.getNumVertices(); i++) { pvaluesShuttle[i*3] = (float)
				 (shuttleObj.getVertices()[i]).x(); pvaluesShuttle[i*3+1] = (float)
				 (shuttleObj.getVertices()[i]).y(); pvaluesShuttle[i*3+2] = (float)
				 (shuttleObj.getVertices()[i]).z(); tvaluesShuttle[i*2] = (float)
				 (shuttleObj.getTexCoords()[i]).x(); tvaluesShuttle[i*2+1] = (float)
				 (shuttleObj.getTexCoords()[i]).y(); nvaluesShuttle[i*3] = (float)
				 (shuttleObj.getNormals()[i]).x(); nvaluesShuttle[i*3+1] = (float)
				 (shuttleObj.getNormals()[i]).y(); nvaluesShuttle[i*3+2] = (float)
				 (shuttleObj.getNormals()[i]).z(); }
		 
		 
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
		gl.glBufferData(GL_ARRAY_BUFFER, vertSphereBuf.limit() * 4, vertSphereBuf, GL_STATIC_DRAW);

		vboDict.put("sphereTextures", 6);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("sphereTextures")]);
		FloatBuffer texSphereBuf = Buffers.newDirectFloatBuffer(tvaluesSphere);
		gl.glBufferData(GL_ARRAY_BUFFER, texSphereBuf.limit() * 4, texSphereBuf, GL_STATIC_DRAW);

		
		vboDict.put("mugPositions",7); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("mugPositions")]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvaluesMug); 
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		vboDict.put("mugTextures",8); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("mugTextures")]); 
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvaluesMug); 
		gl.glBufferData(GL_ARRAY_BUFFER,
		texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		
		vboDict.put("shuttlePositions",9); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("shuttlePositions")]);
		FloatBuffer vertBufShuttle = Buffers.newDirectFloatBuffer(pvaluesShuttle); 
		gl.glBufferData(GL_ARRAY_BUFFER, vertBufShuttle.limit()*4, vertBufShuttle, GL_STATIC_DRAW);
		
		vboDict.put("shuttleTextures",10); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("shuttleTextures")]); 
		FloatBuffer texBufShuttle = Buffers.newDirectFloatBuffer(tvaluesMug); 
		gl.glBufferData(GL_ARRAY_BUFFER,
		texBuf.limit()*4, texBufShuttle, GL_STATIC_DRAW);
		

		/*
		vboDict.put("coinPositions",11); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("coinPositions")]);
		FloatBuffer vertBufCoin = Buffers.newDirectFloatBuffer(pvaluesCoin); 
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		vboDict.put("coinTextures",12); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboDict.get("coinTextures")]); 
		FloatBuffer texBufCoin = Buffers.newDirectFloatBuffer(tvaluesCoin); 
		gl.glBufferData(GL_ARRAY_BUFFER,
		texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		*/

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