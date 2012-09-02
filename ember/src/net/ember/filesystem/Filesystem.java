package net.ember.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import net.ember.logging.Log;

public class Filesystem {

	static String basedir = "data/";
	
	/**
	 * This is just testing code for now.
	 */
	public static void init(){
		
	}
	
	
	
	/**
	 * Returns a RandomAccessFile representing the given game object.
	 * This should be used in nearly all cases, with the exception of native library loader.
	 * @param name
	 * @return
	 */
	public static RandomAccessFile get(String name) throws FileNotFoundException{
		try {
			return new RandomAccessFile(new File(basedir+name),"r");
		} catch (FileNotFoundException e) {
			Log.warn("Unable to find file "+name+"!");
			throw e;
		}
	}	
	/**
	 * Returns a RandomAccessFile representing the given game object.
	 * This should be used in nearly all cases, with the exception of native library loader.
	 * @param name
	 * @return
	 */
	public static RandomAccessFile getRW(String name) throws FileNotFoundException{
	
			return new RandomAccessFile(new File(basedir+name),"rw");
		
	}
	
	/**
	 * Return a File object representing the given game object.
	 * This should be used only to get the location of native libraries or low-level stuff.
	 * Streams, textures etc are better off read from a PAK archive.
	 * @param name
	 * @return
	 */
	public static File getOnDisk(String name){
		File f = new File(name);
		if(!f.exists())
			Log.warn("Unable to locate "+name+ " - behaviour is undefined.");
		return f;
	}



	public static RandomAccessFile getWrite(String name) {
		try {
			return new RandomAccessFile(new File(basedir+name),"rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
