package net.ember.tools.formats.md5;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.ember.filesystem.Filesystem;
import net.ember.logging.Log;

public class MD5ModelFile {

	public static void main(String[] args) throws FileNotFoundException{
		Filesystem.init();
		RandomAccessFile fis = Filesystem.get("nonfree/md5/hellknight/hellknight.md5mesh");
		MD5ModelFile mmf = new MD5ModelFile();
		mmf.read(fis);
		mmf.process();
		try {
			mmf.meshes[0].dump();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.info("Complete.");
	}
	
	
	private void process() {
		for(int i=0;i<meshes.length;i++){
			meshes[i].flipST();
			meshes[i].processVertices(joints);
			meshes[i].processNormals();	
		}
	}


	
	public void read(RandomAccessFile raf){


		String line = null;            
		long n=0;
		try {
			while ((line = raf.readLine()) != null) {
				parse(line,n);
				n++;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		

	}

	private int version;
	private String commandline;
	
	private Section onSection = Section.NONE;
	private int jointOn, faceOn, meshOn;
	private int numJoints, numFaces, numMeshes;
	
	private MD5Mesh[] meshes;
	private MD5Joint[] joints;
	
	private void parse(String line, long n) {
		//N is the line number, from 0 being 1st line


		//Remove multiple spaces
		line=line.replaceAll("\\s{2,}", " "); 

		//Remove tabs to make parsing easier
		line=line.replaceAll("\t", "");

		//System.out.println(line);
		if(line.startsWith("//"))
			return;

		//0th line defines the version number.
		if(n==0){
			//Expecting "MD5Version "...
			if(line.startsWith("MD5Version")){

				version = Integer.parseInt(line.replace("MD5Version ", ""));
				if(version!=10)
					Log.warn("Version "+version+" not supported. May not function as expected.");
				return;
			}
			else{
				Log.err("Not an MD5 file!");
				System.exit(0);
			}
		}

		if(n==1){
			commandline = line.replace("commandline ", "");
			return;
		}

		if(line.startsWith("numJoints")){
			numJoints = Integer.parseInt(line.replace("numJoints ", ""));
			joints = new MD5Joint[numJoints];
			return;
		}
		if(line.startsWith("numMeshes")){
			numMeshes = Integer.parseInt(line.replace("numMeshes ", ""));
			meshes = new MD5Mesh[numMeshes];
			return;
		}
		if(line.replaceAll(" ","").equals("joints{"))
		{
			onSection=Section.JOINTS;
			return;
		}
		if(line.replaceAll(" ","").equals("mesh{"))
		{
			onSection=Section.MESH;
			meshes[meshOn]=new MD5Mesh(meshOn);
			return;
		}
		if(line.replaceAll(" ","").equals("}")){
			if(onSection == Section.MESH)
				meshOn++;
			if(onSection == Section.NONE)
				Log.warn("WTF? We have a close-parentheses closing no section! Line "+n);
			onSection=Section.NONE;
			jointOn=0;
			return;
		}

		switch(onSection){

		case JOINTS:
			joints[jointOn]=new MD5Joint(line,jointOn);
			jointOn++;
			return;		
		case MESH:
			//Could be defining shader/number, or could be a data line
			meshes[meshOn].parse(line);
			break;

		default:
			if(!line.equals(""))Log.warn("Unknown Line encountered! Line is \""+line+"\" , line number "+n);
			break;
		}





	}
	enum Section{
		NONE,
		JOINTS,
		MESH,

		BOUNDS,
		HIERARCHY,
		BASEFRAME,
		FRAME
	}

}
