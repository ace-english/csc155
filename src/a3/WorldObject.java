package a3;

import org.joml.Vector2f;
import org.joml.Vector3f;

public interface WorldObject {
	public int getNumVertices();

	public Vector2f[] getTexCoords();

	public Vector3f[] getNormals();

	public Vector3f[] getVertices();
}
