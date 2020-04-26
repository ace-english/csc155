package a4;

public class Material {
	private float[] ambient, diffuse, specular;
	private float shininess;

	public Material(float[] ambient, float[] diffuse, float[] specular, float shininess) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
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

	public float getShininess() {
		return shininess;
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

	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

}
