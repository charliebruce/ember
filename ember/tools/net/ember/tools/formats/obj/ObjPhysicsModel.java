package net.ember.tools.formats.obj;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import net.ember.data.Type;


public class ObjPhysicsModel {

	private static final int MAX_VERTICES = 1024*1024;
	private static final int MAX_FACES = 1024*1024;
	int numVertices=0;
	int numFaces=0;

	float[] vertices;
	int[] indices;

	public void addVertex(String s) {


		String[] tokens = s.split(" "); 

		float x = Float.parseFloat(tokens[0]);
		float y = Float.parseFloat(tokens[1]);
		float z = Float.parseFloat(tokens[2]);

		vertices[3*numVertices]=x;
		vertices[3*numVertices+1]=y;
		vertices[3*numVertices+2]=z;

		numVertices++;

	}

	public ObjPhysicsModel(){
		vertices = new float[3*MAX_VERTICES];
		indices = new int[3*MAX_FACES];
	}

	public void addFace(String face) {
		//NOTE we subtract one, since obj seems to define the first vertex as 1 not 0.
		String[] split = face.split(" ");
		if(split[0].contains("/")){

			String[] s0 = split[0].split("/");
			String[] s1 = split[1].split("/");
			String[] s2 = split[2].split("/");

			int[] vertices = new int[]{Integer.parseInt(s0[0]),Integer.parseInt(s1[0]),Integer.parseInt(s2[0])};

			indices[3*numFaces]=vertices[0]-1;
			indices[3*numFaces+1]=vertices[1]-1;
			indices[3*numFaces+2]=vertices[2]-1;



		}
		else {
			int[] vertices = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
			indices[3*numFaces]=vertices[0]-1;
			indices[3*numFaces+1]=vertices[1]-1;
			indices[3*numFaces+2]=vertices[2]-1;
		}

		numFaces++;
	}

	public void save() throws IOException {
		//TODO TODO TODO replace with the MUCH (90 vs 350kb) smaller GZip (GZIPOutputStream)
		RandomAccessFile gof = new RandomAccessFile(new File("C:/workspace/workspace/ember/data/regions/0/physics.mesh"),"rw");
		//BufferedWriter bw = new BufferedWriter(new FileWriter(raf.getFD()));

		int numdatas = numVertices*3;
		int numindices = numFaces*3;
		ByteBuffer bb = ByteBuffer.allocateDirect(36).putInt(Type.PhysicsMesh).putInt(0).putInt(numdatas).putInt(numindices);
		for(int i=0;i<5;i++){
			bb.putInt(0);
		}
		bb.rewind();

		gof.getChannel().write(bb);

		ByteBuffer b = ByteBuffer.allocateDirect(numdatas*4);
		for(int i=0;i<numdatas;i++){b.putFloat(vertices[i]);}
		b.rewind();
		gof.getChannel().write(b);

		ByteBuffer b1 = ByteBuffer.allocateDirect(numindices*4);
		for(int i=0;i<numindices;i++){b1.putInt(indices[i]);}
		b1.rewind();
		gof.getChannel().write(b1);

		gof.close();




	}

	public void scale(float scale) {
		for(int i=0;i<vertices.length;i++){
			vertices[i]=vertices[i]*scale;
		}
	}

	

}
