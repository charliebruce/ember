package net.ember.math;

import net.ember.logging.Log;

public class Matrix {

	//TODO finalising this means it can't get changed right?
	public static final float[] identity4 = new float[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
	
	public static final float[] identity3 = new float[]{1,0,0,0,1,0,0,0,1};
	
	
	/**
	 * Matrix multiplication
	 * result = a*b
	 * TODO optimise this! Unroll
	 * @param a
	 * @param b
	 * @return
	 */
	public static float[] multMatrix(float[] a, float[] b) {
	 
	    float[] res = new float[16];
	 
	    for (int i = 0; i < 4; ++i) {
	        for (int j = 0; j < 4; ++j) {
	            res[j*4 + i] = 0.0f;
	            for (int k = 0; k < 4; ++k) {
	                res[j*4 + i] += a[k*4 + i] * b[j*4 + k];
	            }
	        }
	    }
	    return res;
	 
	}
	
	/**
	 * Transpose of a 3x3 matrix
	 * @param input
	 * @return
	 */
	public static float[] transpose3(float[] input){
		return new float[]{input[0],input[3],input[6],input[1],input[4],input[7],input[2],input[5],input[8]};
	}
	/**
	 * Transpose of a 4x4 matrix
	 * @param input
	 * @return
	 */
	public static float[] transpose4(float[] input){
		return new float[]{input[0],input[4],input[8],input[12],input[1],input[5],input[9],input[13],input[2],input[6],input[10],input[14],input[3],input[7],input[11],input[15]};
	}
	/**
	 * Determinant of a 3x3 matrix
	 * @param in
	 * @return
	 */
	public static float det3(float[] in){
		return in[0]*in[4]*in[8]-in[0]*in[5]*in[7]-in[1]*in[3]*in[8]+in[1]*in[5]*in[6]+in[2]*in[3]*in[7]-in[2]*in[4]*in[6];
	}
	public static float det4(float[] m) {
		float value;
		value =
				m[3]*m[6]*m[9]*m[12] - m[2]*m[7]*m[9]*m[12] - m[3]*m[5]*m[10]*m[12] + m[1]*m[7]*m[10]*m[12]+
				m[2]*m[5]*m[11]*m[12] - m[1]*m[6]*m[11]*m[12] - m[3]*m[6]*m[8]*m[13] + m[2]*m[7]*m[8]*m[13]+
				m[3]*m[4]*m[10]*m[13] - m[0]*m[7]*m[10]*m[13] - m[2]*m[4]*m[11]*m[13] + m[0]*m[6]*m[11]*m[13]+
				m[3]*m[5]*m[8]*m[14] - m[1]*m[7]*m[8]*m[14] - m[3]*m[4]*m[9]*m[14] + m[0]*m[7]*m[9]*m[14]+
				m[1]*m[4]*m[11]*m[14] - m[0]*m[5]*m[11]*m[14] - m[2]*m[5]*m[8]*m[15] + m[1]*m[6]*m[8]*m[15]+
				m[2]*m[4]*m[9]*m[15] - m[0]*m[6]*m[9]*m[15] - m[1]*m[4]*m[10]*m[15] + m[0]*m[5]*m[10]*m[15];
		return value;
	} 
	/*
	 * UNKNOWN but believed to be adjoint? adj(m)
	 */
	public static float[] adj3(float[] m){
		return new float[]{(m[8]*m[4]-m[5]*m[7]),-1.0f*(m[8]*m[1]-m[7]*m[2]),m[5]*m[1]-m[4]*m[2]/*3rd cell*/,
				-1.0f*(m[8]*m[3]-m[6]*m[5]),m[8]*m[0]-m[6]*m[2],-1.0f*(m[5]*m[0]-m[3]*m[2]),
				m[7]*m[3]-m[6]*m[4],-1.0f*(m[7]*m[0]-m[6]*m[1]),m[4]*m[0]-m[3]*m[1]};//from dr-lex.be/random/matrix_inv.html
	}
	public static float[] mult3float(float scalar,float[] matrix){
		float[] ret = new float[9];
		//for(int i = 0; i<9;i++){
		//	ret[i]=scalar*matrix[i];
		//}
		ret[0]=scalar*matrix[0];
		ret[1]=scalar*matrix[1];
		ret[2]=scalar*matrix[2];
		ret[3]=scalar*matrix[3];
		ret[4]=scalar*matrix[4];
		ret[5]=scalar*matrix[5];
		ret[6]=scalar*matrix[6];
		ret[7]=scalar*matrix[7];
		ret[8]=scalar*matrix[8];
		
		return ret;
	}
	public static float[] mult4float(float scalar,float[] matrix){
		float[] ret = new float[16];
		//for(int i = 0; i<16;i++){
		//	ret[i]=scalar*matrix[i];
		//}
		ret[0]=scalar*matrix[0];
		ret[1]=scalar*matrix[1];
		ret[2]=scalar*matrix[2];
		ret[3]=scalar*matrix[3];
		ret[4]=scalar*matrix[4];
		ret[5]=scalar*matrix[5];
		ret[6]=scalar*matrix[6];
		ret[7]=scalar*matrix[7];
		ret[8]=scalar*matrix[8];
		ret[9]=scalar*matrix[9];
		ret[10]=scalar*matrix[10];
		ret[11]=scalar*matrix[11];
		ret[12]=scalar*matrix[12];
		ret[13]=scalar*matrix[13];
		ret[14]=scalar*matrix[14];
		ret[15]=scalar*matrix[15];
		return ret;
	}
	/**
	 * Returns the top-left 3x3 submatrix from this 4x4 one
	 */
	public static float[] submatrix3from4(float[] m){
		return new float[]{m[0],m[1],m[2],m[4],m[5],m[6],m[8],m[9],m[10]};
	}
	/**
	 * The inverse of the given 3*3 matrix
	 * ie so that MM^-1=I
	 * @param in
	 * @return
	 */
	public static float[] inverse3(float[] in){
		float detm = det3(in);
		if(detm==0.0f){
			Log.err("We tried to invert a non-invertible matrix!");
			return identity3;//Break it more obviously than a null return.
		}
		//float[] mt=transpose3(in);

		return mult3float((1.0f/detm),adj3(in));//TODO thisis (1.0f/det)*(adj(m))
	}
	
	/**
	 * This matrix is the transpose of the inverse of the 3×3 upper left sub matrix from the modelview matrix.
	 * @param modelview
	 * @return
	 */
	public static float[] normalmatrix(float[] modelview){
		return transpose3(inverse3(submatrix3from4(modelview)));
	}
	
	
		
	/**
	 * Decommissioned but works fine to test.
	 * @param args
	 */
	public static void main(String[] args){
		//Test WORKS
		float[] matrix = new float[]{7,5,9,15,11,3,10,16,2,8,13,17,4,12,14,6};
		long t0 = System.nanoTime();
		for(int i=0;i<100000000;i++){
			float[] inverse = inverse4(matrix);

		}
		long tt=System.nanoTime()-t0;
		System.out.println("1M 4x4 inverses takes "+tt+" which is "+(tt/1000000)+" ms ");
		//System.out.println(inverse[0]+","+inverse[1]+","+inverse[2]+","+inverse[3]+","+inverse[4]+","+inverse[5]+","+inverse[6]+","+inverse[7]+","+inverse[8]);
	}

	/**
	 * Find the inverse of an invertible 4x4 matrix.
	 * 122ms/1M
	 * @param m
	 * @return
	 */
	public static float[] inverse4(float[] m) {
		float[] o = new float[16];
		o[0] = m[6]*m[11]*m[13] - m[7]*m[10]*m[13] + m[7]*m[9]*m[14] - m[5]*m[11]*m[14] - m[6]*m[9]*m[15] + m[5]*m[10]*m[15];
		o[1] = m[3]*m[10]*m[13] - m[2]*m[11]*m[13] - m[3]*m[9]*m[14] + m[1]*m[11]*m[14] + m[2]*m[9]*m[15] - m[1]*m[10]*m[15];
		 o[2] = m[2]*m[7]*m[13] - m[3]*m[6]*m[13] + m[3]*m[5]*m[14] - m[1]*m[7]*m[14] - m[2]*m[5]*m[15] + m[1]*m[6]*m[15];
		  o[3] = m[3]*m[6]*m[9] - m[2]*m[7]*m[9] - m[3]*m[5]*m[10] + m[1]*m[7]*m[10] + m[2]*m[5]*m[11] - m[1]*m[6]*m[11];
		   o[4] = m[7]*m[10]*m[12] - m[6]*m[11]*m[12] - m[7]*m[8]*m[14] + m[4]*m[11]*m[14] + m[6]*m[8]*m[15] - m[4]*m[10]*m[15];
		   o[5] = m[2]*m[11]*m[12] - m[3]*m[10]*m[12] + m[3]*m[8]*m[14] - m[0]*m[11]*m[14] - m[2]*m[8]*m[15] + m[0]*m[10]*m[15];
		   o[6] = m[3]*m[6]*m[12] - m[2]*m[7]*m[12] - m[3]*m[4]*m[14] + m[0]*m[7]*m[14] + m[2]*m[4]*m[15] - m[0]*m[6]*m[15];
		   o[7] = m[2]*m[7]*m[8] - m[3]*m[6]*m[8] + m[3]*m[4]*m[10] - m[0]*m[7]*m[10] - m[2]*m[4]*m[11] + m[0]*m[6]*m[11];
		   o[8] = m[5]*m[11]*m[12] - m[7]*m[9]*m[12] + m[7]*m[8]*m[13] - m[4]*m[11]*m[13] - m[5]*m[8]*m[15] + m[4]*m[9]*m[15];
		   o[9] = m[3]*m[9]*m[12] - m[1]*m[11]*m[12] - m[3]*m[8]*m[13] + m[0]*m[11]*m[13] + m[1]*m[8]*m[15] - m[0]*m[9]*m[15];
		   o[10] = m[1]*m[7]*m[12] - m[3]*m[5]*m[12] + m[3]*m[4]*m[13] - m[0]*m[7]*m[13] - m[1]*m[4]*m[15] + m[0]*m[5]*m[15];
		   o[11] = m[3]*m[5]*m[8] - m[1]*m[7]*m[8] - m[3]*m[4]*m[9] + m[0]*m[7]*m[9] + m[1]*m[4]*m[11] - m[0]*m[5]*m[11];
		   o[12] = m[6]*m[9]*m[12] - m[5]*m[10]*m[12] - m[6]*m[8]*m[13] + m[4]*m[10]*m[13] + m[5]*m[8]*m[14] - m[4]*m[9]*m[14];
		   o[13] = m[1]*m[10]*m[12] - m[2]*m[9]*m[12] + m[2]*m[8]*m[13] - m[0]*m[10]*m[13] - m[1]*m[8]*m[14] + m[0]*m[9]*m[14];
		   o[14] = m[2]*m[5]*m[12] - m[1]*m[6]*m[12] - m[2]*m[4]*m[13] + m[0]*m[6]*m[13] + m[1]*m[4]*m[14] - m[0]*m[5]*m[14];
		   o[15] = m[1]*m[6]*m[8] - m[2]*m[5]*m[8] + m[2]*m[4]*m[9] - m[0]*m[6]*m[9] - m[1]*m[4]*m[10] + m[0]*m[5]*m[10];
		   return mult4float((1.0f/det4(m)),o);
	}

	public static String string4(float[] c){
		return("Matrix <"+c[0]+","+c[1]+","+c[2]+","+c[3]+","+c[4]+","+c[5]+","+c[6]+","+c[7]+","+c[8]+","+c[9]+","+c[10]+","+c[11]+","+c[12]+","+c[13]+","+c[14]+","+c[15]+">");
	}
	
	public static float[] translationMatrix( float x, float y, float z) {
		float[] res = Matrix.identity4;
		res[12]=x;
		res[13]=y;
		res[14]=z;
		return res;
	}
}
