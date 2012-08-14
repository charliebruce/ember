package net.ember.tools.formats.md5;

import net.ember.logging.Log;
import net.ember.math.Quaternion;

public class MD5Joint {


	String name="";
	int parentindex;
	int id;

	//TODO those as vec3 containers?
	float[] pos = new float[3];
	float[] orientation;
	
	/**
	 * Create a Joint object from the line in the MD5 file which defines it.
	 * @param line The line which defines the joint.
	 */
	public MD5Joint(String line, int myid) {
		id=myid;
		//Constructed from "origin"-1 ( 0 0 0 ) ( -0.5 -0.5 -0.5 ) // 
		//<string:name> <int:parentIndex> ( <vec3:position> ) ( <vec3:orientation> )
		String name = line.substring(1, line.indexOf("\"", 1));
		
		//TODO this
		String[] datas = line.split(" ");
		int parent = Integer.parseInt(datas[0].substring(1+line.indexOf("\"", 1)));
		
		//2-4 are posxyz 789 are orxyz assuming 1 is openbracket, 5)6( 10)
		if(!(datas[1].equals("(")&&datas[5].equals(")")&&datas[6].equals("(")&&datas[10].equals(")")))
			Log.warn("Joint data not split properly.");
		
		pos[0] = Float.parseFloat(datas[2]);
		pos[1] = Float.parseFloat(datas[3]);
		pos[2] = Float.parseFloat(datas[4]);
		orientation=Quaternion.fromxyz(Float.parseFloat(datas[7]), Float.parseFloat(datas[8]), Float.parseFloat(datas[9]));
		
		Log.debug("Creating joint "+name+", id "+id+", parent "+parent+", quaternion "+Quaternion.toString(orientation));
		
		
		//Orientation is a quaternion, generate that now? from normalisation?
	}
	public boolean isRoot(){
		return(parentindex==-1);
	}

}
