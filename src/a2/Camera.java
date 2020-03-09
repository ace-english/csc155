package a2;

import org.joml.Vector3f;

public class Camera {
	private Vector3f location, target;
	private Vector3f u, v, n;
	// v is up
	// n is forward
	// u is right
	// private Matrix4f uvn;

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
		calculateUVN();
	}

	public Camera(float x, float y, float z) {
		this.location = new Vector3f(x, y, z);
		this.target = new Vector3f(0f, 0f, 0f);
		calculateUVN();
	}

	public Camera(Vector3f location) {
		this.location = location;
		this.target = new Vector3f(0f, 0f, 0f);
		calculateUVN();
	}

	private void calculateUVN() {
		n = (target.sub(location)).normalize();
		v = new Vector3f(n);
		v.cross(new Vector3f(0, 1, 0).cross(v));
		u = new Vector3f(n);
		u.cross(v);
		// uvn = new Matrix4f(u.x, u.y, u.z, 0, v.x, v.y, v.z, 0, n.x, n.y, n.z, 0, 0,
		// 0, 0, 1);
		// System.out.printf("(%.1f,%.1f,%.1f)\n", location.x, location.y, location.z);
		System.out.printf("u: (%.1f,%.1f,%.1f)\n", u.x, u.y, u.z);
		System.out.printf("v: (%.1f,%.1f,%.1f)\n", v.x, v.y, v.z);
		System.out.printf("n: (%.1f,%.1f,%.1f)\n", n.x, n.y, n.z);
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
		move(1, v);
	}

	public void moveDown() {
		move(-1, v);
	}

	public void moveLeft() {
		move(1, u);
	}

	public void moveRight() {
		move(-1, u);
	}

	public void moveIn() {
		move(1, n);
	}

	public void moveOut() {
		move(-1, n);
	}

	public void move(int direction, Vector3f vec) {
		System.out.printf("(%.1f,%.1f,%.1f)\n", vec.x, vec.y, vec.z);
		// add 0.1 times current v to location
		Vector3f delta = new Vector3f(vec);
		delta.mul(0.1f * direction);
		location = location.add(delta);
	}
}
