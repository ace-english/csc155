package a4;

public abstract class Light {
	private float[] ambient, diffuse, specular;

	public Light() {
		ambient = new float[] { 1.0f, 1.0f, 1.0f };
		diffuse = new float[] { 1.0f, 1.0f, 1.0f };
		specular = new float[] { 1.0f, 1.0f, 1.0f };
	}

	public Light(float[] ambient, float[] diffuse, float[] specular) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}

	public float[] getAmbient() {
		return ambient;
	}

	public float[] getDiffuse() {
		return diffuse;
	}

	public float[] getSpecular() {
		return specular;
	}

	public void setAmbient(float[] ambient) {
		this.ambient = ambient;
	}

	public void setDiffuse(float[] diffuse) {
		this.diffuse = diffuse;
	}

	public void setSpecular(float[] specular) {
		this.specular = specular;
	}

	public void add(float scalar) {
		ambient[0] += scalar;
		ambient[1] += scalar;
		ambient[2] += scalar;
		diffuse[0] += scalar;
		diffuse[1] += scalar;
		diffuse[2] += scalar;
		specular[0] += scalar;
		specular[1] += scalar;
		specular[2] += scalar;

	}

}
