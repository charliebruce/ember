package net.ember.tools.formats.obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class ObjMaterialFile {

	String currentName;
	String ka;
	float ns=8.0f;//default
	
	private List<ObjMaterial> materials = new ArrayList<ObjMaterial>();
	
	public ObjMaterialFile(){
		
	}



	public void save() throws IOException {

		addCurrent();
		
		
		BufferedWriter bw = null;
		
		try{
			bw = new BufferedWriter(new FileWriter("data/regions/0/materials.mmd"));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return;
		}

		
		for(ObjMaterial m: materials){
			//System.out.println("Outputting material "+m.name);
			m.write(bw);
		}

		bw.close();
		

	}



	public void addCurrent() {
		ObjMaterial m = new ObjMaterial(currentName);
		m.ka = ka;
		m.ns=ns;
		materials.add(m);
		currentName=null;
		ka = null;
	}



	public void newMaterial(String string) {
		if(currentName!=null)
			addCurrent();
		currentName = string;
	}



	public void setka(String string) {
		ka=string;
	}



	public void setShininess(float parseFloat) {
		ns = parseFloat;
		//TODO phong to blinn phong etc?
	}
	
	

	

}
