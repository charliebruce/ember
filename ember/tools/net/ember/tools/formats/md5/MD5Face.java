package net.ember.tools.formats.md5;

public class MD5Face {

	int id;
	int[] indices;
	public MD5Face(int id, int[] vindex) {
		this.id=id;
		this.indices = vindex;
	}

	//public float[] position, normal, tangent;
	public float[] normal;
	
}
