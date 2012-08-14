package net.ember.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import net.ember.logging.Log;

/**
 * Load a static model file into a StaticModel object
 * TODO make generic to load phys meshes, compute tangents/normals/bones too?
 * @author Charlie
 *
 */
public class OrderedStaticModelLoader {

	

	public static Model get(FileInputStream gis, int[] params, String name) {
		OrderedStaticModel m = new OrderedStaticModel();
		
		try {
			
			int mats = params[0];
			byte[] bt = new byte[mats];
			
	        gis.read( bt, 0, (mats));
	        String mat = new String(bt,"UTF-8");
	        
	        m.matnames = mat.split("\n");
	        
	        
	        
	        Log.info("Reading "+(params[1]/4)+" floats and "+params[2]/4+" offsets.");
			
			bt = new byte[params[1]];
			m.data = new float[params[1]/4];
			
	        gis.read( bt, 0, params[1]);
	        ByteBuffer bb = ByteBuffer.wrap(bt);
	        bb.rewind();
	        bb.asFloatBuffer().get(m.data);
			
	        
	        
	        
	        m.offsets = new int[params[2]/4];
	        byte[] bt2 = new byte[params[2]];
	        
	        gis.read(bt2,0,params[2]);
	        ByteBuffer bb2 = ByteBuffer.wrap(bt2);
	        bb2.rewind();
	        bb2.asIntBuffer().get(m.offsets);
	        
	        
	        m.lengths = new int[params[2]/4];
	        byte[] bt3 = new byte[params[2]];
	        
	        gis.read(bt3,0,params[2]);
	        ByteBuffer bb3 = ByteBuffer.wrap(bt3);
	        bb3.rewind();
	        bb3.asIntBuffer().get(m.lengths);
	        
	        //TODO generate tangents to save bandwidth and space?
			
			
		} catch (IOException e) {
			Log.info("Encountered an IOException when reading static model!");
			e.printStackTrace();
			return null;
		}
		m.name = name;
		return m;
	}
}
