package net.ember.tools.formats.obj;

public class Vertex {

	public float x,y,z;
	public float s=0.0f,t=0.0f;
	public float nx=0.0f,ny=0.0f,nz=0.0f;
	public Vertex(float mx,float my, float mz){
		x=mx;
		y=my;
		z=mz;
		
	}
	public void scale(float scale) {
		x=scale*x;
		y=scale*y;
		z=scale*z;
	}
}
