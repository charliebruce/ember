package net.ember.tools.formats.vmt;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import net.ember.filesystem.Filesystem;
import net.ember.graphics.Material;

/**
 * Valve Material Type
 * @author Peter
 *
 */
public class VmtImporter {

	public static void main(String[] args){
		
		try {
			get(Filesystem.get("nonfree/mat.vmt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static Material get(RandomAccessFile f){
		Material m = new Material();
		
		
		
		
		
	}
	
	
}
