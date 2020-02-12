package notes1;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

//starting at ch 2.4
public class Code extends JFrame implements GLEventListener
{ 	
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[ ] = new int[1];

   public Code()
   { setTitle("Chapter2 - program1");
		 setSize(600, 400);
		 setLocation(200, 200);
		 myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
   }

   public void display(GLAutoDrawable drawable)
   { 	GL4 gl = (GL4) GLContext.getCurrentGL();		
   		gl.glUseProgram(renderingProgram);
   		gl.glPointSize(70.0f);
   		gl.glDrawArrays(GL_POINTS, 0, 1);

   }

   public static void main(String[ ] args)
   { new Code();
   }

   public void init(GLAutoDrawable drawable)
   { GL4 gl = (GL4) GLContext.getCurrentGL();
   		 renderingProgram = createShaderProgram();
   		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
}

   private int createShaderProgram()
   { GL4 gl = (GL4) GLContext.getCurrentGL();
		// arrays to collect GLSL compilation status values.
		// note: one-element arrays are used because the associated JOGL calls require arrays.
		int[ ] vertCompiled = new int[1];
		int[ ] fragCompiled = new int[1];
		int[ ] linked = new int[1];


		 String vshaderSource[ ] =
		 { "#version 430 \n",
			 "void main(void) \n",
			 "{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n",
		};

		 String fshaderSource[ ] =
		 { "#version 430 \n",
			 "out vec4 color; \n",
			 "void main(void) \n",
			 "{ if (gl_FragCoord.x < 200) color = vec4(1.0, 0.0, 0.0, 1.0); else color = vec4(0.0, 1.0, 1.0, 1.0);\r\n" + 
			 "}\r\n" + 
			 " \n",
		};

		 int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		 gl.glShaderSource(vShader, 3, vshaderSource, null, 0); // 3 is the count of lines of source code
		gl.glCompileShader(vShader);
		checkOpenGLError();
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] != 1)
		   { System.out.println("vertex compilation failed.");
				printShaderLog(vShader);
		}


		 int fShader=gl.glCreateShader(GL_FRAGMENT_SHADER);
		 gl.glShaderSource(fShader, 4, fshaderSource, null, 0); // 4 is the count of lines of source code
		gl.glCompileShader(fShader);
		checkOpenGLError();
		   gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		   if (fragCompiled[0] != 1)
		   { System.out.println("fragment compilation failed.");
				printShaderLog(fShader);
		   }

		   if ((vertCompiled[0] != 1) || (fragCompiled[0] != 1))
		   { System.out.println("\nCompilation error; return-flags:");
				 System.out.println(" vertCompiled = " + vertCompiled[0] + " ; fragCompiled = " + fragCompiled[0]);
		}

/*
			vshaderSource = readShaderSource("vertShader.glsl");
			 fshaderSource = readShaderSource("fragShader.glsl");
			 gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
			 gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
*/
		   
		 int vfProgram = gl.glCreateProgram();
		 gl.glAttachShader(vfProgram, vShader);
		 gl.glAttachShader(vfProgram, fShader);
		gl.glLinkProgram(vfProgram);
		checkOpenGLError();
		   gl.glGetProgramiv(vfProgram, GL_LINK_STATUS, linked,0);
		   if (linked[0] != 1)
		   { System.out.println("linking failed.");
				printProgramLog(vfProgram);
		}


		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		 return vfProgram;
}


   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
   public void dispose(GLAutoDrawable drawable) { }
   
   private void printShaderLog(int shader)
   { GL4 gl = (GL4) GLContext.getCurrentGL();
      int[ ] len = new int[1];
      int[ ] chWrittn = new int[1];
      byte[ ] log = null;

      // determine the length of the shader compilation log
      gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
      if (len[0] > 0)
      { log = new byte[len[0]];
   		 gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
   		 System.out.println("Shader Info Log: ");
   		 for (int i = 0; i < log.length; i++)
   		 { System.out.print((char) log[i]);
   }}}

   void printProgramLog(int prog)
   { GL4 gl = (GL4) GLContext.getCurrentGL();
      int[ ] len = new int[1];
      int[ ] chWrittn = new int[1];
      byte[ ] log = null;

      // determine the length of the program linking log
      gl.glGetProgramiv(prog,GL_INFO_LOG_LENGTH,len, 0);
      if (len[0] > 0)
      { log = new byte[len[0]];
   		 gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0,log, 0);
   		 System.out.println("Program Info Log: ");
   		 for (int i = 0; i < log.length; i++)
   		 { System.out.print((char) log[i]);
   }}}

   boolean checkOpenGLError()
   { GL4 gl = (GL4) GLContext.getCurrentGL();

      boolean foundError = false;
      GLU glu = new GLU();
      int glErr = gl.glGetError();
      while (glErr != GL_NO_ERROR)
      { System.err.println("glError: " + glu.gluErrorString(glErr));
   		 foundError = true;
   		 glErr = gl.glGetError();
      }
      return foundError;
   }
   
   private String[ ] readShaderSource(String filename)
   { Vector<String> lines = new Vector<String>();
		 Scanner sc;
		 String[ ] program;
		try
		 { sc = new Scanner(new File(filename));
			 while (sc.hasNext())
			 {lines.addElement(sc.nextLine());
			 }
			 program = new String[lines.size()];
			 for (int i = 0; i < lines.size(); i++)
			 {program[i] = (String) lines.elementAt(i) + "\n";
			 }
		}

      catch (IOException e)
		 {System.err.println("IOException reading file: " + e);
			 return null;
		}
		 return program;
} }


   

