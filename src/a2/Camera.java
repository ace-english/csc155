package a2;

import org.joml.Matrix4f;
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

	public void moveUp() {
		translate(1, v);
	}

	public void moveDown() {
		translate(-1, v);
	}

	public void moveLeft() {
		translate(-1, u);
	}

	public void moveRight() {
		translate(1, u);
	}

	public void moveIn() {
		translate(1, n);
	}

	public void moveOut() {
		translate(-1, n);
	}

	public void panLeft() {
		pan(-1);
	}

	public void panRight() {
		pan(1);
	}

	public void pitchUp() {
		pitch(1);
	}

	public void pitchDown() {
		pitch(-1);
	}

	public void translate(int direction, Vector3f vec) {
		// add 0.1 times current v to location
		Vector3f delta = new Vector3f(vec);
		delta.mul(0.1f * direction);
		location = location.add(delta);
	}

//holding down left should not move the up arrow
//check calculations
	public void rotate(int direction, Vector3f axis) {
		Vector3f[] vectors = { u, v, n };
		for (int i = 0; i < 3; i++) {
			v = vectors[i];
			if (!(v == axis)) {
				v.rotateAxis(0.05f, axis.x, axis.y, axis.z);
			}
		}
	}

	public void pan(int direction) {
		n.rotateAxis(0.05f * direction, v.x, v.y, v.z);
		u.rotateAxis(0.05f * direction, v.x, v.y, v.z);
	}

	public void pitch(int direction) {
		n.rotateAxis(0.05f * direction, u.x, u.y, u.z);
		v.rotateAxis(0.05f * direction, u.x, u.y, u.z);
	}

	public Matrix4f getUVM() {
		return new Matrix4f(u.x, u.y, u.z, 0, v.x, v.y, v.z, 0, n.x, n.y, n.z, 0, 0, 0, 0, 1);
	}

	public Matrix4f getC() {
		return new Matrix4f(1.0f, 0.0f, 0.0f, -location.x, 0.0f, 1.0f, 0.0f, -location.y, 0.0f, 0.0f, 1.0f, -location.z,
				0.0f, 0.0f, 0.0f, 1.0f);
	}

	public Matrix4f getMV() {
		return new Matrix4f(getUVM().mul(new Matrix4f(1.0f, 0.0f, 0.0f, -location.x, 0.0f, 1.0f, 0.0f, -location.y,
				0.0f, 0.0f, 1.0f, -location.z, 0.0f, 0.0f, 0.0f, 1.0f)));
	}
}
