package net.ember.graphics;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import net.ember.logging.Log;

/**
 * A set of properties, fed to a shader, which set the properties in the gbuffer to allow for a certain appearance.
 * 
 * Supports:
 * Albedo (Colour)
 * Normal Map
 * Specular Mask and Exponent Mask (maps or numbers, could use alpha of normal a la valve $phong)
 * Mat id?/Fresnel?
 * @author Charlie
 *
 */
public class Material {

	
	String matname = "";
	public int[] ids = new int[4];

	private int numTextures;
	
	public Material(String name) {
		matname = name;
		//Log.info("New material: "+matname);
		
	}

	public String getName(){
		return matname;
	}
	
	public String normalMap="";
	public String albedoMap="";
	
	public boolean onGraphics=false;

	/**
	 * Push the textures onto the GPU
	 * @param gl
	 */
	void loadIntoGraphics(GL gl){
		
		
		Log.info("Loading material into graphics: " + matname);
		
		
		ids[0]=Graphics.renderer.nullAlbedoTexture;
		ids[1]=Graphics.renderer.nullNormalTexture;
		
		numTextures=0;
		if(!albedoMap.equals(""))
			numTextures++;
		if(!normalMap.equals(""))
			numTextures++;
		
		for(int i=0;i<4;i++){
			ids[i]=0;
		}
		
		if(numTextures>0){
			int[] myids = new int[numTextures];
			gl.glGenTextures(numTextures, myids,0);	
			int i=0;
			if(!albedoMap.equals("")){
				ids[0]=myids[i];
				Graphics.renderer.loadTextureData(albedoMap, ids[0], gl);
				i++;
			}
			if(!normalMap.equals("")){
				ids[1]=myids[i];
				Graphics.renderer.loadTextureData(normalMap, ids[1], gl);
				i++;
			}
		
		}
				
				
		onGraphics=true;
		
	}
	
	/**
	 * Release the textures from the graphics memory.
	 * @param gl
	 */
	void unloadFromGraphics(GL gl){
		if(onGraphics){
			if(numTextures>0){
				int[] texIds = new int[numTextures];
				int at=0;
				for(int i=0;i<4;i++){
					if(ids[i]!=0){
						texIds[at]=ids[i];
						at++;
					}
				}
				
				gl.glDeleteTextures(numTextures, texIds,0);
			}
			onGraphics=false;
		}
		
		
	}
	
	public boolean useNormalMap(){
		return(!normalMap.equals(""));
	}
	public boolean useAlbedoMap(){
		return(!albedoMap.equals(""));
	}
	
	
	/**
	 * The "database" of materials defined in the material files.
	 */
	private static Map<String,Material> materials = new HashMap<String,Material>();
	
	public static Material get(String name){
		return materials.get(name);
	}
	public static void put(Material mat){
		if(!materials.containsKey(mat.matname)){
			materials.put(mat.matname, mat);
		}else{
			Log.warn("The material already exists, resetting.");
			Material old = materials.get(mat.matname);
			if(old.onGraphics){
				Log.err("Oh shit. We're trying to overwrite a material which is loaded. We're leaking a texture here!");
			}
			materials.remove(mat.matname);
			old = null;
			materials.put(mat.matname,mat);
		}
				
	}
}
