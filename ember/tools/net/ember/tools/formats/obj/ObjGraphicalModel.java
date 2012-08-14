package net.ember.tools.formats.obj;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ember.data.Type;
import net.ember.logging.Log;

public class ObjGraphicalModel {

	int numVertices=0;
	int numFaces=0;
	int currentTc=0;
	int currentNormal=0;
	String currentMaterial = "";


	/*
	 * Vertices, numbered starting at 0 not 1.
	 */
	private Map<Integer, Vertex> vertices = new HashMap<Integer,Vertex>();

	/*
	 * Texcoords, numbered starting at 0 not 1.
	 */
	private Map<Integer, Texcoord> texcoords = new HashMap<Integer, Texcoord>();
	
	/*
	 * Normals, numbered starting at 0 not 1.
	 */
	private Map<Integer, Normal> normals = new HashMap<Integer, Normal>();
	
	
	/*
	 * Chunks are bits of data, in triangles (xyz,st,nxyz,txyzw) with the same material applied to them.
	 */
	private Map<String, Chunk> chunks = new HashMap<String, Chunk>();
	
	public void addVertex(String s) {


		String[] tokens = s.split(" "); 

		float x = Float.parseFloat(tokens[0]);
		float y = Float.parseFloat(tokens[1]);
		float z = Float.parseFloat(tokens[2]);

		vertices.put(numVertices, new Vertex(x,y,z));
		numVertices++;

	}

	public ObjGraphicalModel(){
		
	}

	public void addFace(String face) {
		Chunk current = chunks.get(currentMaterial);
		//NOTE we subtract one, since obj seems to define the first vertex as 1 not 0.
		String[] split = face.split(" ");
		if(split[0].contains("/")){

			String[] s0 = split[0].split("/");
			String[] s1 = split[1].split("/");
			String[] s2 = split[2].split("/");

			int[] thevertices = new int[]{Integer.parseInt(s0[0]),Integer.parseInt(s1[0]),Integer.parseInt(s2[0])};

			int tc0=-1,tc1=-1,tc2=-1;
			if(s0.length>1){
				tc0=Integer.parseInt(s0[1])-1;
				tc1=Integer.parseInt(s1[1])-1;
				tc2=Integer.parseInt(s2[1])-1;
			}
			int n0=-1,n1=-1,n2=-1;
			if(s0.length>2){
				n0=Integer.parseInt(s0[2])-1;
				n1=Integer.parseInt(s1[2])-1;
				n2=Integer.parseInt(s2[2])-1;
			}
			float s0s=0.0f,s0t=0.0f,s1s=0.0f,s1t=0.0f,s2s=0.0f,s2t=0.0f;
			if(tc0!=-1){
				s0s=texcoords.get(tc0).s;
				s0t=texcoords.get(tc0).t;
				s1s=texcoords.get(tc1).s;
				s1t=texcoords.get(tc1).t;
				s2s=texcoords.get(tc2).s;
				s2t=texcoords.get(tc2).t;
			}
			
			
			Vertex2 a = new Vertex2(vertices.get(thevertices[0]-1));
			Vertex2 b = new Vertex2(vertices.get(thevertices[1]-1));
			Vertex2 c = new Vertex2(vertices.get(thevertices[2]-1));

			a.s=s0s;
			a.t=s0t;
			b.s=s1s;
			b.t=s1t;
			c.s=s2s;
			c.t=s2t;
			
			if(n0!=-1){
				a.nx=normals.get(n0).nx;
				a.ny=normals.get(n0).ny;
				a.nz=normals.get(n0).nz;
				b.nx=normals.get(n1).nx;
				b.ny=normals.get(n1).ny;
				b.nz=normals.get(n1).nz;
				c.nx=normals.get(n2).nx;
				c.ny=normals.get(n2).ny;
				c.nz=normals.get(n2).nz;
			}
			//v vt vn
			current.newFace(a,b,c);

		}
		else {
			Log.info("ignoring face "+face+" BECAUSE IT IS UNTEXTURED.");
			//No tcs, not going to use it.
			/*int[] vertices = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
			indices[3*numFaces]=vertices[0]-1;
			indices[3*numFaces+1]=vertices[1]-1;
			indices[3*numFaces+2]=vertices[2]-1;*/
		}

		numFaces++;
	}

	public void save(RandomAccessFile gof) throws IOException {
		
		//Link texcoords to vertices using faces
		
		//That loses info but should be ok and simplifies/enlarges/interleaves the arrays
		
		
		//WRITE AN ORDERED STATIC MODEL
		
		
		
		//TODO TODO TODO replace with the MUCH (90kb vs 350kb) smaller GZip (GZIPOutputStream)
		//RandomAccessFile gof = new RandomAccessFile(new File("C:/workspace/workspace/ember/data/regions/0/graphics.mesh"),"rw");
		//BufferedWriter bw = new BufferedWriter(new FileWriter(raf.getFD()));

		//GL_TRIANGLES layout going xyz,st,n(xyz),t(xyzw)
		Log.info("Writing "+numVertices+" vertices to disk.");

		//Builds rawdata, rawoffsets
		buildData(scale);
		
		
		
		StringBuilder dat = new StringBuilder();
		for(int i=0;i<rawmaterials.length;i++){
			dat.append(rawmaterials[i]+"\n");
		}
		String mtl = dat.toString();
		byte[] data = mtl.getBytes("UTF-8");
		
		/*
		 * Write the header.
		 */
		ByteBuffer bb = ByteBuffer.allocateDirect(36).putInt(Type.OrderedStaticModel).putInt(data.length).putInt(rawdata.length*4).putInt(rawoffsets.length*4);
		for(int i=0;i<5;i++){
			bb.putInt(0);//Fill the remaining bytes.
		}
		bb.rewind();
		gof.getChannel().write(bb);


		/*
		 * Write the strings.
		 */
		ByteBuffer strings = ByteBuffer.allocateDirect(data.length);
		strings.put(data);
		strings.rewind();
		gof.getChannel().write(strings);
		
		/*
		 * Write the raw data.
		 */
		ByteBuffer b = ByteBuffer.allocateDirect(rawdata.length*4);
		//b.order(ByteOrder.LITTLE_ENDIAN);
		for(int i=0;i<rawdata.length;i++){b.putFloat(rawdata[i]);}
		b.rewind();
		gof.getChannel().write(b);

		/*
		 * Write the offsets.
		 */
		ByteBuffer b1 = ByteBuffer.allocateDirect(rawoffsets.length*4);
		for(int i=0;i<rawoffsets.length;i++){b1.putInt(rawoffsets[i]);}
		b1.rewind();
		gof.getChannel().write(b1);
		
		/*
		 * Write the lengths.
		 */
		ByteBuffer b2 = ByteBuffer.allocateDirect(rawlengths.length*4);
		for(int i=0;i<rawlengths.length;i++){b2.putInt(rawlengths[i]);}
		b2.rewind();
		gof.getChannel().write(b2);

		
		/*
		 * Output.
		 */
		gof.close();




	}

	private float scale=1.0f;
	public void scale(float s) {
		scale = s;
	}

	float[] rawdata;
	int[] rawoffsets;
	int[] rawlengths;
	String[] rawmaterials;
	void buildData(float scale2){
		
		//Total num of floats
		int floatal=0;
		Collection<Chunk> mychunks = chunks.values();
		
		for(Chunk c: mychunks){
			floatal = floatal + c.length();
		}
		Log.info("floatal is "+floatal+" coll has "+mychunks.size());
		rawdata = new float[floatal];
		
		float[] minnn;
		int offset=0;
		for(Chunk c: mychunks){
			minnn=c.output(scale2);
			//System.out.println("MTL" + c.mat+" minnn contains "+minnn[0]+","+minnn[1]+","+minnn[2]+","+minnn[3]+","+minnn[4]+ " write to "+offset);
			for(int i=0;i<minnn.length;i++){
				rawdata[offset+i]=minnn[i];
			}
			offset=offset+minnn.length;
		}
		
		rawmaterials = new String[mychunks.size()];
		int on=0;
		for(Chunk c: mychunks){
			rawmaterials[on]=c.mat;
			on++;
		}
		
		
		
		
		rawoffsets = new int[mychunks.size()];
		rawlengths = new int[mychunks.size()];
		on=0;
		rawoffsets[0]=0;
		for(Chunk c: mychunks){
			rawlengths[on]=c.length()/12;
			if(on<mychunks.size()-1){
				rawoffsets[on+1]=rawoffsets[on]+rawlengths[on];//THIS?
			}
			on++;
		}
		//for(int i=0;i<rawlengths.length;i++){
		//	rawlengths[i]=rawlengths[i]/12;
		//}
		
	}
	

	public void addVertexNormal(String string) {
		String[] split = string.split(" ");
		normals.put(currentNormal, new Normal(Float.parseFloat(split[0]),Float.parseFloat(split[1]),Float.parseFloat(split[2])));
		currentNormal++;
	}

	public void useMaterial(String replace) {
		currentMaterial = replace;//For multiple groups, save indices!!!!!
		if(chunks.get(currentMaterial)==null){
			chunks.put(currentMaterial, new Chunk(currentMaterial));
		}
	}

	public void addVertexTexcoord(String string) {
		String[] split = string.split(" ");
		texcoords.put(currentTc, new Texcoord(Float.parseFloat(split[0]),Float.parseFloat(split[1])));
		currentTc++;
	}

}
