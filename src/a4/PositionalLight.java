package a4;

import org.joml.Vector3f;

public class PositionalLight extends Light {
	private Vector3f position;

	public PositionalLight(Vector3f position) {
		super();
		this.position = position;
	}

	public PositionalLight(float[] ambient, float[] diffuse, float[] specular, Vector3f position) {
		super(ambient, diffuse, specular);
		this.position = position;
	}

	public PositionalLight(float[] ambient, float[] diffuse, float[] specular) {
		super(ambient, diffuse, specular);
		this.position = new Vector3f(0f, 0f, 0f);
	}

	public PositionalLight() {
		super();
		this.position = new Vector3f(0f, 0f, 0f);
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

}
