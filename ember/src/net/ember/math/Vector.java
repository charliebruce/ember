package net.ember.math;

public class Vector {

	/**
	 * Returns AXB of 3D vectors A,B.
	 * a-down, b-right gives c-OUT OF SCREEN
	 * @param a
	 * @param b
	 * @return
	 */
	public static final float[] crossProduct3(float []a, float[] b) {
		float[]res = new float[3];
	    res[0] = a[1] * b[2]  -  b[1] * a[2];
	    res[1] = a[2] * b[0]  -  b[2] * a[0];
	    res[2] = a[0] * b[1]  -  b[0] * a[1];
	    return res;
	}
	
	public static final float dotProduct3(float[] a, float[] b){
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	 
	// Normalize a vec3
	public static final float[] normalize3(float[] a) {
	 
	    float mag = 1.0f/((float) Math.sqrt(a[0] * a[0]  +  a[1] * a[1]  +  a[2] * a[2]));
	    //Save two division-by-zero checks in favour of an additional multiply. efficient?
	    return new float[]{a[0]*mag,a[1]*mag,a[2]*mag};
	}

	public static final double[] normalize3d(double[] a) {
	 
	    double mag = 1.0D/Math.sqrt(a[0] * a[0]  +  a[1] * a[1]  +  a[2] * a[2]);
	    return new double[]{a[0]*mag,a[1]*mag,a[2]*mag};
	}
	
	
	
	public static float[] normalise3(float[] vec) {
		return normalize3(vec);
	}
	
	public static double[] normalise3d(double[] vecd) {
		return normalize3d(vecd);
	}
	public static final float[] multByMatrix4(float[] v, float[] m) {
		float[] res = new float[4];
		res[0]=v[0]*m[0]+v[1]*m[4]+v[2]*m[8]+v[3]*m[12];
		res[1]=v[0]*m[1]+v[1]*m[5]+v[2]*m[9]+v[3]*m[13];
		res[2]=v[0]*m[2]+v[1]*m[6]+v[2]*m[10]+v[3]*m[14];
		res[3]=v[0]*m[3]+v[1]*m[7]+v[2]*m[11]+v[3]*m[14];
		
		return res;
	}

	public static float[] multiplyScalar3(float s, float[] normal) {
		
		return new float[]{normal[0]*s,normal[1]*s,normal[2]*s};
	}
	
	
	public static double[] multiplyScalar3d(double s, double[] normal) {
		return new double[]{normal[0]*s,normal[1]*s,normal[2]*s};
	}


	/**
	 * R=A-B
	 * @param a
	 * @param b
	 * @return
	 */
	public static float[] subtract3(float[] a, float[] b) {
		return new float[]{a[0]-b[0],a[1]-b[1],a[2]-b[2]};
	}
	public static double[] subtract3d(double[] a, double[] b) {
		return new double[]{a[0]-b[0],a[1]-b[1],a[2]-b[2]};
	}

	public static String toString3(float[] position) {
		return new StringBuilder("V[").append(position[0]).append(",").append(position[1]).append(",").append(position[2]).append("]").toString();
	}

	public static float[] add3(float[] a, float[] b) {
		return new float[]{a[0]+b[0],a[1]+b[1],a[2]+b[2]};
	}
	public static double[] add3d(double[] a, double[] b) {
		return new double[]{a[0]+b[0],a[1]+b[1],a[2]+b[2]};
	}

	public static float[] lowp3(double[] highp3){
		return new float[]{(float) highp3[0],(float) highp3[1],(float) highp3[2]};
	}

	public static String toString3d(double[] position) {
		return new StringBuilder("VD[").append(position[0]).append(",").append(position[1]).append(",").append(position[2]).append("]").toString();
		
	}
}
