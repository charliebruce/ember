package net.ember.tools.formats.md5;

public class MD5Weight {

	int id, joint;
	float bias;
	float[] position;
	
	public MD5Weight(int id, int joint, float bias, float[] pos) {
		this.id = id;
		this.joint = joint;
		this.bias = bias;
		this.position = pos;
	}

}
