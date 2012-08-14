package net.ember.tools.formats.obj;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

	//Faces
	private List<Face> faces = new ArrayList<Face>();
	String mat;
	
	public Chunk(String currentMaterial) {
		mat = currentMaterial;
	}

	public float[] output(float scale2){
		float[] f = new float[length()];
		int i=0;
		for(Face fa: faces){
			fa.output(f,i,scale2);
			i=i+36;
		}
		return f;
	}

	//Numberr of floats I contain.
	public int length() {
		return faces.size()*36;
	}

	public void newFace(Vertex2 a, Vertex2 b, Vertex2 c) {
		faces.add(new Face(a,b,c));
	}

}
