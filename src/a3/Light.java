package a3;

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

}
