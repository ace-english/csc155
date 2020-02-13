package a1;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;

public class Starter extends JFrame implements GLEventListener {
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private float x = 0.0f; // location of triangle
	private float inc = 0.01f; // offset for moving the triangle vert
	GL4 gl;
	private String openGLVersion, JoglVersion, JavaVersion;
	boolean rainbow, vertical, circle;

	public Starter() {
		setTitle("Assignment 1");
		setSize(800, 400);
		setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.setSize(600, 400);
		myCanvas.setLocation(200, 0);
		myCanvas.addGLEventListener(this);
		this.setVisible(true);
		/*
		 * // Define new buttons JButton vertButton = new JButton("Move Vertically");
		 * JButton circleButton = new JButton("Move Radially"); Container
		 * buttonContainer = new Container(); buttonContainer.setSize(200, 400);
		 * 
		 * buttonContainer.setLayout(new FlowLayout()); this.add(buttonContainer);
		 * 
		 * // Add buttons to the frame (and spaces between buttons)
		 * buttonContainer.add(vertButton); buttonContainer.add(circleButton);
		 * 
		 * this.add(buttonContainer);
		 */
		this.add(myCanvas);
		this.setVisible(true);

		Animator animtr = new Animator(myCanvas);
		animtr.start();
	}

	public void display(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT); // clear the background to black, each time
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);
		x += inc;
		// move the triangle along y axis
		if (x > 1.0f)
			inc = -0.01f; // switch to moving the triangle down
		if (x < -1.0f)
			inc = 0.01f; // switch to moving the triangle up
		int offsetLoc = gl.glGetUniformLocation(renderingProgram, "Ty"); // retrieve pointer to "offset"
		gl.glProgramUniform1f(renderingProgram, offsetLoc, x);
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);

	}

	public static void main(String[] args) {
		new Starter();
	}

	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		openGLVersion = gl.glGetString(GL_VERSION);
		JavaVersion = System.getProperty("java.version");
		JoglVersion = Package.getPackage("com.jogamp.opengl").getImplementationVersion();

		System.out.println("OpenGL version: " + openGLVersion);
		System.out.println("JOGL version: " + JoglVersion);
		System.out.println("Java version: " + JavaVersion);
		
		float vertices[] = {
			    // positions         // colors
			     0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f,   // bottom right
			    -0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f,   // bottom left
			     0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f    // top 
			};   
	}

	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		// arrays to collect GLSL compilation status values.
		// note: one-element arrays are used because the associated JOGL calls require
		// arrays.
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		String[] vshaderSource = readShaderSource("src/a1/vertShader.glsl");
		String[] fshaderSource = readShaderSource("src/a1/fragShader.glsl");

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(vShader);
		checkOpenGLError();
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] != 1) {
			System.out.println("vertex compilation failed.");
			printShaderLog(vShader);
		}

		gl.glCompileShader(fShader);
		checkOpenGLError();
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] != 1) {
			System.out.println("fragment compilation failed.");
			printShaderLog(fShader);
		}

		if ((vertCompiled[0] != 1) || (fragCompiled[0] != 1)) {
			System.out.println("\nCompilation error; return-flags:");
			System.out.println(" vertCompiled = " + vertCompiled[0] + " ; fragCompiled = " + fragCompiled[0]);
		}

		int vfProgram = gl.glCreateProgram();
		gl.glAttachShader(vfProgram, vShader);
		gl.glAttachShader(vfProgram, fShader);
		gl.glLinkProgram(vfProgram);
		checkOpenGLError();
		gl.glGetProgramiv(vfProgram, GL_LINK_STATUS, linked, 0);
		if (linked[0] != 1) {
			System.out.println("linking failed.");
			printProgramLog(vfProgram);
		}

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		return vfProgram;
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void dispose(GLAutoDrawable drawable) {
	}

	private void printShaderLog(int shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	void printProgramLog(int prog) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the program linking log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}

	private String[] readShaderSource(String filename) {
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
			sc.close();
		}

		catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
		return program;
	}

}
