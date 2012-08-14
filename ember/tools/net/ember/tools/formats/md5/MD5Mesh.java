package net.ember.tools.formats.md5;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.vecmath.Vector3f;

import net.ember.data.Type;
import net.ember.logging.Log;
import net.ember.math.Quaternion;

public class MD5Mesh {

	int id;
	String tex;

	int numverts, numfaces, numWeights;

	MD5Vertex[] vertices;
	MD5Face[] faces;
	MD5Weight[] weights;
	public MD5Mesh(int meshOn) {
		id = meshOn;
	}

	public void parse(String line) {


		if(line.startsWith("shader"))
		{
			tex = line.replace("shader \"", "").replace("\"","");
			Log.debug("Texture "+tex+" is required. Check extension, list it as an asset, go.");
			return;
		}

		if(line.startsWith("numverts"))
		{
			numverts = Integer.parseInt(line.replace("numverts ",""));
			Log.debug("Verts: "+numverts);
			vertices = new MD5Vertex[numverts];
			return;
		}
		if(line.startsWith("numtris"))
		{
			numfaces = Integer.parseInt(line.replace("numtris ",""));
			Log.debug("Tris: "+numfaces);
			faces = new MD5Face[numfaces];
			return;
		}

		if(line.startsWith("vert ")){
			//	vert id ( s t ) startweight countweight
			String[] datas = line.replace("vert ","").split(" ");
			int id = Integer.parseInt(datas[0]);
			float s = Float.parseFloat(datas[2]);
			float t = Float.parseFloat(datas[3]);
			int startweight = Integer.parseInt(datas[5]);
			int countweight = Integer.parseInt(datas[6]);

			MD5Vertex v = new MD5Vertex(id,s,t,startweight,countweight);
			vertices[id]=v;

			return;
		}

		if(line.startsWith("tri ")){
			//tri triIndex vertIndex[0] vertIndex[1] vertIndex[2]

			String[] datas = line.replace("tri ","").split(" ");
			int id = Integer.parseInt(datas[0]);
			int vindex[]=new int[3];
			//System.out.println("s: "+line.replace("tri ", ""));
			vindex[0]=Integer.parseInt(datas[1]);
			vindex[1]=Integer.parseInt(datas[2]);
			vindex[2]=Integer.parseInt(datas[3]);

			MD5Face t = new MD5Face(id,vindex);
			faces[id]=t;

			return;
		}
		if(line.startsWith("numweights")){

			numWeights = Integer.parseInt(line.replace("numweights ",""));
			Log.debug("Tris: "+numWeights);
			weights = new MD5Weight[numWeights];
			return;


		}
		if(line.startsWith("weight ")){
			String[] datas = line.replace("weight ","").split(" ");
			int id = Integer.parseInt(datas[0]);
			int joint = Integer.parseInt(datas[1]);
			float bias = Float.parseFloat(datas[2]);
			float[] pos = new float[3];
			pos[0] = Float.parseFloat(datas[4]);
			pos[1] = Float.parseFloat(datas[5]);
			pos[2] = Float.parseFloat(datas[6]);

			weights[id]= new MD5Weight(id,joint,bias,pos);
			return;
		}
		if(line.equals(" ")||line.equals("")||line.equals("  ")){
			return;
		}

		System.out.println("UNABLE TO HANDLE LINE "+line);
	}

	public void processNormals() {
		for(int i=0;i<faces.length;i++){
			Vector3f v0 = new Vector3f(vertices[faces[i].indices[0]].position);
			Vector3f v1 = new Vector3f(vertices[faces[i].indices[1]].position);
			Vector3f v2 = new Vector3f(vertices[faces[i].indices[2]].position);
			v1.sub(v0);
			v2.sub(v0);
			Vector3f normal = new Vector3f();
			normal.cross(v1, v2);
			normal.normalize();
			faces[i].normal=new float[]{normal.x,normal.y,normal.z};
		}
		for(int i=0;i<vertices.length;i++){
			vertices[i].normal = new Vector3f();

			for(int f=0;f<faces.length;f++){
				if(faces[f].indices[0]==i||faces[f].indices[1]==i||faces[f].indices[2]==i){
					vertices[i].normal.add(new Vector3f(faces[f].normal));
				}
			}
			vertices[i].normal.normalize();

		}
	}

	public void processVertices(MD5Joint[] joints) {
		for(int i=0;i<faces.length;i++){
			for(int j=0;j<3;j++){
				MD5Vertex v = vertices[faces[i].indices[j]];
				v.calculatePosition(weights, joints);
			}
		}
	}

	public void dump() throws IOException {
		//TODO TODO TODO replace with the MUCH (90 vs 350kb) smaller GZip (GZIPOutputStream)
		RandomAccessFile gof = new RandomAccessFile(new File("C:/workspace/workspace/ember/data/test/hellknight.mesh"),"rw");
		//BufferedWriter bw = new BufferedWriter(new FileWriter(raf.getFD()));
	
		int numfloats = faces.length*3*12;
		int numoffsets = 1;
		ByteBuffer bb = ByteBuffer.allocateDirect(36).putInt(Type.SkinnedModel).putInt(0).putInt(numfloats).putInt(numoffsets);
		for(int i=0;i<5;i++){
			bb.putInt(0);
		}
		bb.rewind();
		gof.getChannel().write(bb);
	
		//NOTE THAT TODO REWIND MD5s for culling, or am i doing it wrong?
		//DirectX and OpenGL evaluate winding order with respect to the direction along which the triangle is being viewed. You must have that axis to evaluate winding order. For DX, CCW triangles are not drawn. For OGL, CW triangles are not drawn. (unless culling is turned off)
		
		ByteBuffer b = ByteBuffer.allocateDirect(numfloats*4);
		for(int i=0;i<faces.length;i++){
			/*
			 * READ IN REVERSE ORDER TO ENSURE CULLING WORKS
			 * Also STs are 1.0f-t'd because of the signs.
			 */
			MD5Vertex v = vertices[faces[i].indices[2]];
			b.putFloat(v.position[0]);
			b.putFloat(v.position[1]);
			b.putFloat(v.position[2]);
			b.putFloat(v.s);
			b.putFloat(v.t);
			b.putFloat(-v.normal.x);
			b.putFloat(-v.normal.y);
			b.putFloat(-v.normal.z);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
	
			v = vertices[faces[i].indices[1]];
			b.putFloat(v.position[0]);
			b.putFloat(v.position[1]);
			b.putFloat(v.position[2]);
			b.putFloat(v.s);
			b.putFloat(v.t);
			b.putFloat(-v.normal.x);
			b.putFloat(-v.normal.y);
			b.putFloat(-v.normal.z);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
	
	
			v = vertices[faces[i].indices[0]];
			b.putFloat(v.position[0]);
			b.putFloat(v.position[1]);
			b.putFloat(v.position[2]);
			b.putFloat(v.s);
			b.putFloat(v.t);
			b.putFloat(-v.normal.x);
			b.putFloat(-v.normal.y);
			b.putFloat(-v.normal.z);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
			b.putFloat(0.0f);
	
			//bw.write(vertices[faces[i].indices[0]].position[0]+","+vertices[faces[i].indices[0]].position[1]+","+vertices[faces[i].indices[0]].position[2]+"\n");
			//bw.write(vertices[faces[i].indices[1]].position[0]+","+vertices[faces[i].indices[1]].position[1]+","+vertices[faces[i].indices[1]].position[2]+"\n");
			//bw.write(vertices[faces[i].indices[2]].position[0]+","+vertices[faces[i].indices[2]].position[1]+","+vertices[faces[i].indices[2]].position[2]+"\n");
		}
		
		b.rewind();
		gof.getChannel().write(b);
		gof.close();
		//bw.flush();
		//bw.close();
	
	}

	public void flipST() {
		for(int i=0;i<vertices.length;i++){
			vertices[i].flipT();
		}
	}


}
