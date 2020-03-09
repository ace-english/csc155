package a2;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private Vector3f location, target;
	private Vector3f u, v, n;
	// v is up
	// n is forward
	// u is right
	private Matrix4f uvn;

	public Vector3f getU() {
		return u;
	}

	public Vector3f getV() {
		return v;
	}

	public Vector3f getN() {
		return n;
	}

	public Camera() {
		this.location = new Vector3f(0f, 0f, 12f);
		this.target = new Vector3f(0f, 0f, 0f);
		uvn = new Matrix4f();
		calculateUVN();
	}

	public Camera(float x, float y, float z) {
		this.location = new Vector3f(x, y, z);
		this.target = new Vector3f(0f, 0f, 0f);
		uvn = new Matrix4f();
		calculateUVN();
	}

	public Camera(Vector3f location) {
		this.location = location;
		this.target = new Vector3f(0f, 0f, 0f);
		uvn = new Matrix4f();
		calculateUVN();
	}

	private void calculateUVN() {
		n = target.sub(location).normalize();
		v = n.cross(new Vector3f(0, 1, 0).cross(n));
		u = n.cross(v);
		uvn = new Matrix4f(u.x, u.y, u.z, 0, v.x, v.y, v.z, 0, n.x, n.y, n.z, 0, 0, 0, 0, 1);
		System.out.printf("(%.1f,%.1f,%.1f)\n", location.x, location.y, location.z);
	}

	public void moveTo(Vector3f location) {
		this.location = location;
	}

	public void lookAt(Vector3f target) {
		this.target = target;
	}

	public Vector3f getLocation() {
		return location;
	}

	public Matrix4f getLookAt() {
		return uvn;
	}

	public void moveHorizontal(float dx) {
		location = location.add(dx, 0f, 0f);
		calculateUVN();
	}

	public void moveDepth(float dz) {
		location = location.add(0f, 0f, dz);
	}

	public void pan() {

		calculateUVN();
	}

	public void moveUp() {
		location = location.add(0f, 0.1f, 0f);
	}

	public void moveDown() {
		location = location.add(0f, -0.1f, 0f);

	}
}
