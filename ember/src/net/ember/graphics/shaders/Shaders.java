package net.ember.graphics.shaders;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import net.ember.graphics.Renderer;
import net.ember.logging.Log;

/**
 * A class to load and remember shaders (which are all loaded at first run, for now).
 * @author Charlie
 *
 */
public class Shaders {

	public static SimpleLightShader simpleLightShader;
	public static SimpleTextureShader simpleTextureShader;
	public static PointLightShader pointLight;
	public static StaticMeshShaderNormalMapped smsnm;
	public static SimpleSkyboxShader sss;
	
	static List<Shader> shaders;
	
	
	
	public static void load(GL2 gl){
		shaders = new LinkedList<Shader>();
		
		simpleTextureShader = new SimpleTextureShader();
		shaders.add(simpleTextureShader);
		
		simpleLightShader = new SimpleLightShader();
		shaders.add(simpleLightShader);
		
		//pointLight  = new PointLightShader();
		//shaders.add(pointLight);
		
		//sss  = new SimpleSkyboxShader();
		//shaders.add(sss);
		
		smsnm = new StaticMeshShaderNormalMapped();
		shaders.add(smsnm);
		
		
		for(Shader s: shaders){
			s.load(gl);
			Log.debug("Shader "+s.getName()+" loaded.");
		}
	
	}



	public static void unload(GL2 gl) {
		for(Shader s: shaders){
			s.unload(gl);
		}
	}
}
