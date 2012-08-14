package net.ember.tools.formats.obj;

import java.io.BufferedWriter;
import java.io.IOException;

public class ObjMaterial {

	public ObjMaterial(String string) {
		name = string;
	}

	public String name;
	public String ka;
	public float ns;

	public void write(BufferedWriter bw) throws IOException {
		bw.write(name+" {"+"\n");
		
		if(ka!=null)
			bw.write("diffusemap "+ka+"\n");
		
		bw.write("specpower "+ns+"\n");
		
		//For now kill speculars entirely.
		bw.write("specatten 0.0\n");
		
		
		bw.write("}"+"\n");
	}
	
	
	
}
