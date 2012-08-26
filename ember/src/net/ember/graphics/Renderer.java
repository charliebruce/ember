package net.ember.graphics;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;


import net.ember.client.Client;
import net.ember.data.Model;
import net.ember.filesystem.Filesystem;
import net.ember.graphics.shaders.Shaders;
import net.ember.logging.Log;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * The renderer used for the graphics.
 * TODO:
 * Code
 * HashMap implementation for tex/data lookup/resx mgmt (prevent tex/model geometry loading twice etc)
 * Material system/ideas
 * Tex atlas?
 * 
 * @author Charlie
 *
 */
public class Renderer implements GLEventListener {

	/**
	 * Fullscreen VBO - just scales from -1 to 1, XY
	 */
	static int fsvbo;

	static int loadTex;




	/**
	 * Display the initial load screen?
	 */
	public boolean initialLoadScreen=true;



	/**For releases, this should use IDs not texture names.*/
	public static Map<String,Material> loadedMaterials = new HashMap<String,Material>();
	public static Map<String,Model> loadedModels = new HashMap<String,Model>();
	public static Map<String,Font> loadedFonts = new HashMap<String,Font>();


	public boolean loadData=false;

	public int nullAlbedoTexture;
	public int nullNormalTexture;


	@Override
	public void display(GLAutoDrawable glad) {
		Log.debug("Display.");
		GL2 gl = glad.getGL().getGL2();
		assertNoError(gl);
		
		//Distinctive red-brown sky.
		gl.glClearColor(0.5f,0.3f,0.2f,0.0f);
		gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		//gl.glEnable(GL.GL_DEPTH_TEST);
		//gl.glUseProgram(Shaders.smsnm.id);
		//m.draw(gl);

		gl.glDisable(GL.GL_DEPTH_TEST);
		Log.info("Depth test disabled.");
		assertNoError(gl);
		
		long t0 = System.nanoTime();
		if(!initialLoadScreen)
		{DeferredRender.deferredPipeline(gl);}
		else
		{Log.info("Preload");assertNoError(gl);preloadScreen(gl);Log.info("Finished preload");assertNoError(gl);}
		
		/**
		 * Determine which render pipeline we're using and render.
		 */
		
		//Low Spec - low-end PCs.
		//High priority lights, ugly alpha blending, cheap water, low-res textures.
		
		
		//Medium-spec - mid-range PCs and Ouya.
		//High and medium priority lights, nicer alpha blending, higher res textures, nicer meshes.
		
		//High-spec - higher-end PCs, future devices.
		//All lights, nice alpha. Deferred pipeline, maximum resolution everything, pretty water, DoF etc?
		
		/**
		 * Overlay, UI, messages
		 */
		
		
		
		
		/**
		 * Menus
		 */
		
		//Upload set number of textures/datas this frame (spread out for performance)
		//uploadData(gl);

		long t1 = System.nanoTime()-t0;
		if(loadData){
			Client.loadManager.loadData(gl);
			loadData=false;
		}
		long t2 = System.nanoTime()-(t1+t0);
		
		//Log.debug("t1 "+t1+", t2 "+t2);
		//Remove unwanted data too.
	}




	public void renderDebugScreen(GL2 gl){
		gl.glUseProgram(Shaders.simpleTextureShader.id);
		gl.glUniform4fv(Shaders.simpleTextureShader.posdimLocation, 1, new float[]{0.0f,0.0f,1.0f,1.0f}, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, DeferredRender.deferredTextures[0]);


		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  fsvbo);
		gl.glVertexAttribPointer(Shaders.simpleTextureShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		gl.glEnableVertexAttribArray(Shaders.simpleTextureShader.vertexLocation);

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		//gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);

	}
	public void preloadScreen(GL2 gl){
		Log.info("Drawing the preload screen.");
		gl.glUseProgram(Shaders.simpleTextureShader.id);
		gl.glUniform4fv(Shaders.simpleTextureShader.posdimLocation, 1, new float[]{0.0f,0.0f,1.0f,1.0f}, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, loadTex);
		Log.info("Bound textures.");
		assertNoError(gl);

		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  fsvbo);
		Log.info("Bound buffer.");
		assertNoError(gl);
		
		gl.glVertexAttribPointer(Shaders.simpleTextureShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		gl.glEnableVertexAttribArray(Shaders.simpleTextureShader.vertexLocation);
		Log.info("Arrays bound.");
		assertNoError(gl);
		
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		Log.info("Array drawn.");
		
		//gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		assertNoError(gl);
		
	}


	public void drawOverlays(GL2 gl){

	}




	@Override
	public void dispose(GLAutoDrawable arg0) {

		Log.info("Disposing of OpenGL resources.");
		GL2 gl = arg0.getGL().getGL2();

		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);

		/**
		 * Remove all textures/materials.
		 */
		for(Material m: loadedMaterials.values()){
			Log.debug("Unloading material " +m.matname);
			m.unloadFromGraphics(gl);
			m=null;//TODO does this make sense?
		}
		loadedMaterials.clear();

		gl.glDeleteTextures(1, new int[]{loadTex},0);
		
		/**
		 * Remove all VBO data.
		 */
		gl.glDeleteBuffers(1, new int[]{fsvbo}, 0);

		for(Model m: loadedModels.values()){
			m.unloadFromGraphics(gl);
			m=null;//TODO does this make sense?
		}
		loadedModels.clear();

		/**
		 * Unload shader programs.
		 */
		Shaders.unload(gl);

		/**
		 * Unload resources used by the pipeline 
		 */
		//DeferredRender.unload(gl);
		
		
		Log.info("Disposed of OpenGL resources.");
	}

	/**
	 * Initialise the resources used - these should be tiny to allow a load screen to be swiftly shown.
	 */
	@Override
	public void init(GLAutoDrawable arg0) {
		
		/**
		 * Configure the GL instance.
		 */
		GL2 gl = arg0.getGL().getGL2();
		gl.setSwapInterval(1);
		Log.debug("Swap interval is 1.");
		assertNoError(gl);
		//gl.glEnable(GL3.GL_TEXTURE);
		
				
		/**
		 * Load the preload screen
		 */
		int[] texs = new int[3];
		gl.glGenTextures(3, texs,0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		loadTextureData("textures/loading.png",texs[0],gl);
		loadTex=texs[0];
		loadTextureData("textures/null.png",texs[1],gl);
		nullAlbedoTexture=texs[1];
		loadTextureData("textures/nullnormal.png",texs[2],gl);
		nullNormalTexture=texs[2];

		Log.debug("Loading base textures.");
		assertNoError(gl);
		
		
		/**
		 * Load the VBO used when showing textures.
		 */
		fsvbo = Utils.loadFullscreenVBO(gl);
		Log.info("FS VBO Load.");
		assertNoError(gl);
		/**
		 * Load the shaders.
		 */
		Shaders.load(gl);
		Log.info("Shaders loaded.");
		assertNoError(gl);
		
		//TODO the below should be done at another time for speed. Get the load screen up quickly.
		/**
		 * Configure the depth tests.
		 */
		//gl.glDepthFunc(GL.GL_LEQUAL);//TODO verify this

		
		
		
		
		//DeferredRender.createBuffers(gl);
	}



	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int width, int height) {
		Graphics.camera.width=width;
		Graphics.camera.height=height;

		Graphics.camera.recalculateProjection();

		//DeferredRender.resizeBuffers(arg0.getGL().getGL2());
		//Resize buffers here.
	}

	/**
	 * Load a texture file immediately into the given texture object, with the default mipmapping/sampling settings. 
	 * @param filename
	 * @param textureId
	 * @param gl
	 */
	void loadTextureData(String filename, int textureId, GL gl){
		Log.info("Loading "+filename);
		try {
			//assertNoError(gl);
			
			gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
			//Renderer.assertNoError(gl);
			
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);//Default to GL_Nearest for performance. GL_Linear bilerps
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);//Other option is to GL_NEAREST it up and clip to nearest pixel
			//gl.glTexParameterf(GL.GL_TEXTURE_2D, GL3.GL_GENERATE_MIPMAP, GL.GL_TRUE);

			//Renderer.assertNoError(gl);
			
			RandomAccessFile file;
			try{
			file = Filesystem.get(filename);
			}
			catch(FileNotFoundException e){
				Log.warn("Loading a dud texture!");
				file = Filesystem.get("textures/null.png");
				filename = "textures/null.png";
			}
			FileDescriptor fd = file.getFD();
			FileInputStream fis = new FileInputStream(fd);

			//NOTE FAILS WITH TARGA TODO THIS 

			//if(!filename.toLowerCase().endsWith("tga")){
			//	int format = GL3.GL_RGB;
			//	BufferedImage bi = ImageIO.read(new BufferedInputStream(fis));
			//	if(bi.getColorModel().hasAlpha())
			//		format = GL3.GL_RGBA;
			//	byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData(); 
			//	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, format/*rgba*/, bi.getWidth(), bi.getHeight(), 0, format, GL.GL_UNSIGNED_BYTE,ByteBuffer.wrap(data));
			//}else{
				TextureData d = TextureIO.newTextureData(gl.getGLProfile(), fis, true, filename.substring(filename.length()-3, filename.length()));
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, d.getInternalFormat(), d.getWidth(), d.getHeight(), 0, d.getPixelFormat(), GL.GL_UNSIGNED_BYTE, d.getBuffer());
				//assertNoError(gl);
				
			//}

			gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
			assertNoError(gl);
			
		} catch (IOException e1) {
			Log.warn("Unable to load texture: "+e1.getMessage());
			e1.printStackTrace();
			return;
		}
		//Log.info("Successful load of "+filename);
	}

	/**
	 * Load a texture immediately into the given texture object, with the default mipmapping/sampling settings.
	 * This method will accept TextureData which has been pre-loaded, and so might be best for speed when loading a future texture.
	 * @param td
	 * @param textureId
	 * @param gl
	 */
	void loadTextureData(TextureData td, int textureId, GL gl){
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);//Default to GL_Nearest for performance. GL_Linear bilerps
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);//Other option is to GL_NEAREST it up and clip to nearest pixel
		//gl.glTexParameterf(GL.GL_TEXTURE_2D, GL3.GL_GENERATE_MIPMAP, GL.GL_TRUE);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, td.getInternalFormat(), td.getWidth(), td.getHeight(), 0, td.getPixelFormat(), GL.GL_UNSIGNED_BYTE, td.getBuffer());

		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		assertNoError(gl);
	}


	public Material getMaterial(String string, GL gl) {
		Material m;
		m = loadedMaterials.get(string);
		if(m!=null) return m;
		m = Material.get(string);
		if(m==null) {Log.warn("Material "+string+" has not been defined."); return null;}//TODO return a blank?
		Log.warn("Late load of material "+m.matname);
		m.loadIntoGraphics(gl);
		loadedMaterials.put(m.matname, m);
		return m;
	}




	public Model getModel(String string) {
		Model m;
		m = loadedModels.get(string);
		return (m==null?null:m);//TODO an emergency-load function in place of the null
	}





	public void loadMaterial(Material m2, GL2 gl) {
		if(loadedMaterials.containsValue(m2)) return;
		m2.loadIntoGraphics(gl);
		assertNoError(gl);
		loadedMaterials.put(m2.matname, m2);
	}



	public void loadModel(Model m, GL2 gl) {
		if(loadedModels.containsValue(m)) return;
		m.loadIntoGraphics(gl);
		assertNoError(gl);
		loadedModels.put(m.name, m);
	}



	/*
	 * Reduces redundant state calls esp when drawing multiple objects sharing buffers.
	 */

	int lastVbo = -1;
	public void bindBuffer(GL2 gl, int glArrayBuffer, int vboId) {
		if(glArrayBuffer == GL.GL_ARRAY_BUFFER){
			if(lastVbo!=vboId){
				gl.glBindBuffer(glArrayBuffer, vboId);
				lastVbo=vboId;
			}
		}
		else{
			gl.glBindBuffer(glArrayBuffer, vboId);
		}
	}
	int lastTex = -1;
	public void bindTexture(GL2 gl, int glTexture2d, int texId) {
		if(glTexture2d==GL.GL_TEXTURE_2D){
			if(lastTex!=texId){
				gl.glBindTexture(glTexture2d, texId);
				lastTex=texId;
			}
		}
		else{
			gl.glBindTexture(glTexture2d, texId);
		}
	}
	
	
	public static void assertNoError(GL gl){
		int i=0;
		while((i=gl.glGetError())!=0){
			switch(i){
			case GL.GL_INVALID_ENUM:
				Log.warn("OpenGL threw an invalid enum error.");
				return;
			case GL.GL_INVALID_OPERATION:
				Log.warn("OpenGL threw an invalid operation error.");
				return;
			case GL.GL_INVALID_VALUE:
				Log.warn("OpenGL threw an invalid value error.");
				return;
			}
		}
		
	}
	
	
	
	
}
