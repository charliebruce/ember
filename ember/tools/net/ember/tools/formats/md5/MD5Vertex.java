package net.ember.tools.formats.md5;

import javax.vecmath.Vector3f;

import net.ember.math.Quaternion;

public class MD5Vertex {

	/**
	 * Generated once completely loaded joints, vertices - bind pose.
	 */
	public float[] position;
	
	public int id, startweight, countweight;
	public float s,t;

	public Vector3f normal;
	
	public MD5Vertex(int id, float s, float t, int startweight, int countweight) {
		this.id = id;
		this.startweight = startweight;
		this.countweight = countweight;
		this.s=s;
		this.t=t;
	}

	public void calculatePosition(MD5Weight[] weights, MD5Joint[] joints) {
		position = new float[3];
		position[0]=0.0f;
		position[1]=0.0f;
		position[2]=0.0f;
		
		for(int k=0;k<countweight;k++){
			MD5Weight w = weights[startweight+k];
			MD5Joint jo = joints[w.joint];
			//weights[]
			float[] vv = Quaternion.rotateAPoint3(jo.orientation,w.position);
			//float[] vv = w.position;
			//System.out.println("id "+face.id+" vid "+v.id+"VV "+vv[0]+" bias "+w.bias);
			position[0]+=(vv[0]+jo.pos[0])*w.bias;
			position[1]+=(vv[1]+jo.pos[1])*w.bias;//rot@ 90deg upwards...
			position[2]+=(vv[2]+jo.pos[2])*w.bias;
			
		}//End weighting
		
	}

	public void flipT() {
		t=1.0f-t;
	}

}
