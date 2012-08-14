package net.ember.tools.formats.obj;

public class Face {

	private Vertex2 ma,mb,mc;
	public Face(Vertex2 a, Vertex2 b, Vertex2 c) {
		ma=a;
		mb=b;
		mc=c;
	}
	
	public void output(float[] f, int i, float s) {
		
		//TODO generate tangents.
		float tx=0.0f,ty=0.0f,tz=0.0f,tw=0.0f;
		
		
		f[i]=ma.x*s;
		f[i+1]=ma.y*s;
		f[i+2]=ma.z*s;
		f[i+3]=ma.s;
		f[i+4]=ma.t;
		//nx,y,z
		f[i+5]=ma.nx;
		f[i+6]=ma.ny;
		f[i+7]=ma.nz;
		//tanx,y,z,w
		f[i+8]=tx;
		f[i+9]=ty;
		f[i+10]=tz;
		f[i+11]=tw;
		
		f[i+12]=mb.x*s;
		f[i+12+1]=mb.y*s;
		f[i+12+2]=mb.z*s;
		f[i+12+3]=mb.s;
		f[i+12+4]=mb.t;
		//nx,y,z
		f[i+12+5]=mb.nx;
		f[i+12+6]=mb.ny;
		f[i+12+7]=mb.nz;
		//tbnx,y,z,w
		f[i+12+8]=tx;
		f[i+12+9]=ty;
		f[i+12+10]=tz;
		f[i+12+11]=tw;
		
		
		f[i+24]=mc.x*s;
		f[i+24+1]=mc.y*s;
		f[i+24+2]=mc.z*s;
		f[i+24+3]=mc.s;
		f[i+24+4]=mc.t;
		//nx,y,z
		f[i+24+5]=mc.nx;
		f[i+24+6]=mc.ny;
		f[i+24+7]=mc.nz;
		//tcnx,y,z,w
		f[i+24+8]=tx;
		f[i+24+9]=ty;
		f[i+24+10]=tz;
		f[i+24+11]=tw;
		
		
	}


}
