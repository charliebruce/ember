package net.ember.math;

public class Quaternion {

	//4 components: w,x,y,z 0-3
	//W-First!
	/**
	 * Multiply the quatrnions - This is in a specific order!
	 * Example:
	 * R=Q.P.Q'
	 * float[] R = Quaternion.multiply(Quaternion.conjugate(Q),Quaternion.multiply(P,Q));
	 * @param a
	 * @param b
	 * @return
	 */
	public static float[] multiply(float[] a, float[] b){
		float[] r = new float[4];
		r[0] = (a[0] * b[0]) - (a[1] * b[1]) - (a[2] * b[2]) - (a[3] * b[3]);
		r[1] = (a[1] * b[0]) + (a[0] * b[1]) + (a[2] * b[3]) - (a[3] * b[2]);
		r[2] = (a[2] * b[0]) + (a[0] * b[2]) + (a[3] * b[1]) - (a[1] * b[3]);
		r[3] = (a[3] * b[0]) + (a[0] * b[3]) + (a[1] * b[2]) - (a[2] * b[1]);
		return r;
	}
	
	
	//Is the inverse of an unit quat
	public static float[] conjugate(float[] a){

		return new float[]{a[0],-1.0f*a[1],-1.0f*a[2],-1.0f*a[2]};

	}
	
	public static float magnitude(float[] q){
		return (float) Math.sqrt(q[0]*q[0]+q[1]*q[1]+q[2]*q[2]+q[3]*q[3]);
	}
	
	/**
	 * Return a unit-length version of the given quaternion.
	 * @param q
	 * @return
	 */
	public static float[] normalise(float[] q){
		float mag = magnitude(q);
		return new float[]{q[0]*mag,q[1]*mag,q[2]*mag,q[3]*mag};
	}

	/**
	 * Generate the quaternion from x,y,z, assuming it's unit length.
	 * Used for importing MD5 models.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static float[] fromxyz(float x, float y, float z){
		float t=1.0f-(x*x)-(y*y)-(z*z);
		float w=0.0f;
		if(t>0.0f)
			w=(float) Math.sqrt(t);
			
		return new float[]{w,x,y,z};
		
	}
	
	public static float[] vec3toquat(float[] vec){
		return new float[]{0.0f,vec[0],vec[1],vec[2]};
	}
	
	/**
	 * Spherical Linear Interpolation
	 * @param qa
	 * @param qb
	 * @param t
	 * @return
	 */
	public static float[] slerp(float[] qa, float[] qb, double t) {
		// quaternion to return
		float[] qm = new float[4];
		// Calculate angle between them.
		double cosHalfTheta = qa[0]* qb[0] + qa[1] * qb[1] + qa[2] * qb[2] + qa[3] * qb[3];
		// if qa=qb or qa=-qb then theta = 0 and we can return qa
		if (Math.abs(cosHalfTheta) >= 1.0){
			return qa;
		}
		// Calculate temporary values.
		double halfTheta = Math.acos(cosHalfTheta);
		double sinHalfTheta = Math.sqrt(1.0 - cosHalfTheta*cosHalfTheta);
		// TODO if theta = 180 degrees then result is not fully defined
		// we could rotate around any axis normal to qa or qb
		//There are infinitely many paths to rotate by 180 degrees...
		if (Math.abs(sinHalfTheta) < 0.001){
			qm[0] = (float) (qa[0] * 0.5 + qb[0] * 0.5);
			qm[1] = (float) (qa[1] * 0.5 + qb[1] * 0.5);
			qm[2] = (float) (qa[2] * 0.5 + qb[2] * 0.5);
			qm[3] = (float) (qa[3] * 0.5 + qb[3] * 0.5);
			return qm;
		}
		double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
		double ratioB = Math.sin(t * halfTheta) / sinHalfTheta; 
		//calculate Quaternion.
		qm[0] = (float) (qa[0] * ratioA + qb[0] * ratioB);
		qm[1] = (float) (qa[1] * ratioA + qb[1] * ratioB);
		qm[2] = (float) (qa[2] * ratioA + qb[2] * ratioB);
		qm[3] = (float) (qa[3] * ratioA + qb[3] * ratioB);
		return qm;
	}


	public static String toString(float[] quat) {
		return new StringBuilder("Q[").append(quat[0]).append(",").append(quat[1]).append(",").append(quat[2]).append(",").append(quat[3]).append("]").toString();
	}


	public static float[] rotateAPoint3(float[] quat, float[] point) {
		// TODO Auto-generated method stub
		float[] mypoint = new float[]{0.0f,point[0],point[1],point[2]};
		// R=Q.P.Q'
		float[] R = Quaternion.multiply(Quaternion.conjugate(quat),Quaternion.multiply(mypoint,quat));
		//float[] R = Quaternion.multiply(Quaternion.multiply(quat,mypoint),Quaternion.conjugate(quat));
		return new float[]{R[1],R[2],R[3]};//TODO optimise or implement on GPU
		
	}
}
