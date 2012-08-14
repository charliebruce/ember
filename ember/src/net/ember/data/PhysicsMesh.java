package net.ember.data;

import java.nio.ByteBuffer;

/**
 * OLD IDEA:
 * A set of values which, when interpreted correctly, will form some geometry.
 * 
 * This object contains the raw values and how they're laid out.
 * 
 * TODO 
 * Handle size, type:
 * Physics/Graphics
 * 2D/3D
 * Texture coordinates
 * Normals
 * Tangent generation
 * Animations??!
 * Indexed or TRIANGLES or Triangle Strip
 * Import of a large set of binary data - more efficiently than readln and parseFloat (streamreader, getfloat).
 * 
 * This format is a RAW data format, and should be used as such. 
 * On import, for model edits, etc, we should be using Vertices, Faces, etc for easy processing.
 * For the engine, a MODEL format should create MESH()es, but also store info about material, scale, CoM, bones, mass etc.
 * @author Charlie
 *
 */
public class PhysicsMesh {

	/**
	 * The raw data.
	 */
	public float[] data;
	
	/**
	 * Indices.
	 */
	public int[] indices;
	
	
	//float[] mydata = new float[]{0.0f,0.0f,0.0f,1000.0f,0.0f,0.0f,0.0f,0.0f,1000.0f,0.0f,0.0f,0.0f,0.0f,0.0f,1000.0f,1000.0f,0.0f,1000.0f};
	//int[] myindices = new int[]{0,1,2,3,4,5};

	public ByteBuffer indexBuffer;
	public ByteBuffer dataBuffer;
	public int faces;//=2;
	public int points;//=6;

	public boolean loaded = false;
	
	/**
	 * TODO we should eventually save the OptimisedBvh back from Bullet too.
	 */
	//public void lol(){
	//	indexBuffer = ByteBuffer.allocate(4*myindices.length);
	//	indexBuffer.asIntBuffer().put(myindices);
		
	//	dataBuffer = ByteBuffer.allocate(4*mydata.length);
	//	dataBuffer.asFloatBuffer().put(mydata);
		
//	}
		
}
