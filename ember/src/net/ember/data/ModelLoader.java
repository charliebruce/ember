package net.ember.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import net.ember.logging.Log;

public class ModelLoader {

	private static final int MAX_PARAMS=8;
	
	public static Model get(RandomAccessFile f, String name){
	
		FileInputStream gis = null;
		int type=0;
		int[] params = new int[8];


		/*
		 * Read the header.
		 */
		try {
			
			gis = new FileInputStream(f.getFD());
			
			byte[] i = new byte[36];
			ByteBuffer loo = ByteBuffer.wrap(i);
			gis.read(i);
			loo.rewind();
			type = loo.getInt();
			for(int n=0;n<MAX_PARAMS;n++){
			params[n]=loo.getInt();}
		}catch(IOException ex){
			Log.warn("IOException loading model.");
			return null;
		}
		
		/*
		 * Select the appropriate loader.
		 */
		
		switch(type){
			case Type.OrderedStaticModel: {
				Log.info("Loading ordered static model!");
				return OrderedStaticModelLoader.get(gis, params, name);
			}
			
			case Type.SkinnedModel: {
				Log.info("Loading skinned model!");
				return SkinnedModelLoader.get(gis,params,name);
			}
			default: {
				Log.err("Unable to load model!");
				return null;
			}
			
		}
		
		
		
	}
	
}
