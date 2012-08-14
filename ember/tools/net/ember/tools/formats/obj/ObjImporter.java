package net.ember.tools.formats.obj;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import net.ember.data.PhysicsMesh;
import net.ember.data.Model;
import net.ember.filesystem.Filesystem;

public class ObjImporter {

	
	
	public static void main(String[] args){
		
		try {//delete them first.
			//objToPhysicsMesh(Filesystem.get("nonfree/sponza/physics.obj"),0.01f);
			//objToGraphicalMesh(Filesystem.get("nonfree/sponza/sponza.obj"),Filesystem.getRW("regions/0/graphics.mesh"), 0.01f);
			objToGraphicalMesh(Filesystem.get("test/unitsphere.obj"),Filesystem.getRW("test/unitsphere.mesh"),0.5f);
			//mtlToMaterial(Filesystem.get("nonfree/sponza/sponza.mtl"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Finished.");
	}
	
	
	
	
	
	
	private static void objToGraphicalMesh(RandomAccessFile raf,
			RandomAccessFile out, float scale) {
		BufferedReader bufferedReader = null;
		ObjGraphicalModel f = new ObjGraphicalModel();
		try {
            
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())));
            String line = null;            
            
            while ((line = bufferedReader.readLine()) != null) {
                parseGraphics(line,f);
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	
		try {
			f.scale(scale);
			f.save(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void mtlToMaterial(RandomAccessFile raf) {
		BufferedReader bufferedReader = null;
		ObjMaterialFile f = new ObjMaterialFile();
		try {
            
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())));
            String line = null;            
            
            while ((line = bufferedReader.readLine()) != null) {
                parseMaterial(line,f);
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	
		try {
			f.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}






	private static void parseGraphics(String line, ObjGraphicalModel into) {
		line = line.replaceAll("\t", "");
		line = line.replaceAll("\\s+", " ");//Regex removes multi-spaces, new lines
			
		if(line.startsWith("mtllib"))
		{System.out.println("Requires material import: "+line);return;}
		if(line.startsWith("usemtl"))
		{into.useMaterial(line.replace("usemtl ",""));return;}
	
		if (line.startsWith("v ")){
			String vertex = line.replace("v ", "");
			//System.out.println(vertices.size());
			into.addVertex((vertex));
			return;
		}
		if (line.startsWith("vn ")){
			String vertex = line.replace("vn ", "");
			//System.out.println(vertices.size());
			into.addVertexNormal((vertex));
			return;
		}		if (line.startsWith("vt ")){
			String vertex = line.replace("vt ", "");
			//System.out.println(vertices.size());
			into.addVertexTexcoord((vertex));
			return;
		}
		
		if (line.startsWith("f ")){
			String face = line.replace("f ","");
			String[ ]split = face.split(" ");
			if(split.length==4){
				//System.err.println("4-face found.");
				//System.out.println(face);
				//4-pointed face. Wound clockwise. Select 1,2,3 and 1,3,4
				into.addFace(split[0]+" "+split[1]+ " "+ split[2]);
				into.addFace(split[0]+" "+split[2]+ " "+ split[3]);
				//faces.add(Face.fromString(split[0]+" "+split[1]+ " "+ split[2], currentMaterial, shadegroup));
				//faces.add(Face.fromString(split[0]+" "+split[2]+ " "+split[3], currentMaterial, shadegroup));
			} else {
			into.addFace(face);
			}
			
			return;
		}
		//Ignore comments
		if(line.startsWith("#")) return;
		
		if(line.equals(" ") || line.equals("")) return;
		System.out.println("Unhandled line "+line);
	}



	private static void parseMaterial(String line, ObjMaterialFile into) {
		line = line.replaceAll("\t", "");
		line = line.replaceAll("\\s+", " ");//Regex removes multi-spaces, new lines
		while(line.startsWith(" "))
			line=line.substring(1);

	
		if (line.startsWith("newmtl ")){
			String material = line.replace("newmtl ", "");
			//System.out.println(vertices.size());
			into.newMaterial((material));
			System.out.println("On material "+material);
			return;
		}
		
		if (line.startsWith("Ns ")){
			String material = line.replace("Ns ", "");
			into.setShininess(Float.parseFloat(material));
			return;
		}
		
		if (line.startsWith("map")){
			System.out.println(line);
			String material = line.replace("map", "");//.toLowerCase();//Lower case a good idea?
			String[] split = material.split(" ");
			//0 is type, 1 is map
			if(split[0].equals("_Ka")){
				into.setka(split[1]);
			}
			return;
		}

		
		


		//Ignore comments
		if(line.startsWith("#")) return;
		
		if(line.equals(" ") || line.equals("")) return;
		
		//System.out.println("Unhandled line in material file: "+line);
	}



	
	
	
	public static void objToPhysicsMesh(RandomAccessFile raf, float scale){
		BufferedReader bufferedReader = null;
		ObjPhysicsModel f = new ObjPhysicsModel();
		try {
            
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())));
            String line = null;            
            
            while ((line = bufferedReader.readLine()) != null) {
                parsePhysics(line,f);
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	
		try {
			f.scale(scale);
			f.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void parsePhysics(String line, ObjPhysicsModel into){
		line = line.replaceAll("\t", "");
		line = line.replaceAll("\\s+", " ");//Regex removes multi-spaces, new lines
			
		
		if (line.startsWith("v ")){
			String vertex = line.replace("v ", "");
			//System.out.println(vertices.size());
			into.addVertex((vertex));
			return;
		}
		
		if (line.startsWith("f ")){
			String face = line.replace("f ","");
			String[ ]split = face.split(" ");
			if(split.length==4){
				//System.err.println("4-face found.");
				//System.out.println(face);
				//4-pointed face. Wound clockwise. Select 1,2,3 and 1,3,4
				into.addFace(split[0]+" "+split[1]+ " "+ split[2]);
				into.addFace(split[0]+" "+split[2]+ " "+ split[3]);
				//faces.add(Face.fromString(split[0]+" "+split[1]+ " "+ split[2], currentMaterial, shadegroup));
				//faces.add(Face.fromString(split[0]+" "+split[2]+ " "+split[3], currentMaterial, shadegroup));
			} else {
			into.addFace(face);
			}
			
			return;
		}
		//Ignore comments
		if(line.startsWith("#")) return;
		
		if(line.equals(" ") || line.equals("")) return;
		
		//System.out.println("OBJ line unhandled "+line);
	}
	
	
}
