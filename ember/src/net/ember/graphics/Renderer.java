package net.ember.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;


import net.ember.client.Client;
import net.ember.data.Model;
import net.ember.filesystem.Filesystem;
import net.ember.game.Entity;
import net.ember.game.World;
import net.ember.graphics.shaders.Shaders;
import net.ember.logging.Log;
import net.ember.math.Matrix;


import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * The GL3-based renderer used for the graphics.
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
	int fsvbo;

	int loadTex;
	/**
	 * The textures to which the deferred buffers are written.
	 */
	int[] deferredTextures;
	int[] lightTextures;

	/**
	 * The framebuffers used in the deferred pipeline.
	 */
	int lightbufferFbo;
	int deferredFbo;



	/**
	 * Display the initial load screen?
	 */
	public boolean initialLoadScreen=true;

	/**
	 * Some constants for multi-render targets.
	 */
	final int[] gbufferBuffers = {GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1, GL3.GL_COLOR_ATTACHMENT2};
	final int[] lightbufferBuffers = {GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1};


	public Map<String,Material> loadedMaterials = new HashMap<String,Material>();
	public Map<String,Model> loadedModels = new HashMap<String,Model>();
	public Map<String,Font> loadedFonts = new HashMap<String,Font>();


	public boolean loadData=false;

	public int nullAlbedoTexture;
	public int nullNormalTexture;


	@Override
	public void display(GLAutoDrawable glad) {
		GL3 gl = glad.getGL().getGL3();

		//Distinctive red-brown sky.
		gl.glClearColor(0.5f,0.3f,0.2f,0.0f);
		gl.glClear (GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		//gl.glEnable(GL.GL_DEPTH_TEST);
		//gl.glUseProgram(Shaders.smsnm.id);
		//m.draw(gl);

		gl.glDisable(GL.GL_DEPTH_TEST);
		
		
		long t0 = System.nanoTime();
		if(!initialLoadScreen)
		{deferredPipeline(gl);}
		else
		{preloadScreen(gl);}
		
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




	/**
	 * The deferred (full pre-pass) method.
	 * 
	 * Buffer layout (fp16):
	 * 0: (4x float16): Diffuse Colour (RGB8 with casting to store two in here)1.5 (+ Specular ColourRGB)1.5+1 for matid or emmisive - emissive could fill specular buffer?? Phong warp?
	 * 1: (4x float16) Normalised Normal X, Y, Z for simplicity (can be -ve) (Gloss or something), (MaterialID&SOMETHING) 
	 * 2: Motion Blur X, Y, (Glow/Emissive?)??????????????????
	 * 3: Depth
	 * 
	 * Shadows:
	 * 
	 * Baked lighting where available/compute n region load?
	 * (+dynamic shadowcasting objects)
	 * When unavailable, cascaded shadow mapping as in http://www.youtube.com/watch?v=IE0yKXtoF0Q&feature=relmfu
	 * also http://www.youtube.com/watch?v=0EjvtQdTHB0&feature=relmfu
	 * @param gl
	 */
	private void deferredPipeline(GL3 gl){

		/**
		 * First, fill the GBuffer, and on another thread create the PVSes for each dynamic-shadowing light.
		 */

		//Use the GBuffer.
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, deferredFbo);
		gl.glDrawBuffers(3, gbufferBuffers, 0);

		//Clear.
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		//Depth test, face culling ON
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL3.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);

		//Use deferred buffer - draw static geometry first..
		gl.glUseProgram(Shaders.smsnm.id);

		//To minimise overdraw, order: Player, skeletons, props, world.


		//For each object send the viewing matrices. Front to back ordering to reduce fragment operations.

		//Identity (no model transform):
		float[] viewProjection = Matrix.multMatrix(Graphics.camera.projection(), Graphics.camera.transformation());
		float[] model=new float[16];

		for(Entity e: World.getVisibleEntities()){
			if(e.model==null) {Log.warn("A visible entity has null model pointer. Continuing without drawing."); continue;}
			model=Utils.transformationMatrix(e.position, e.orientation, e.scale*e.model.scale);
			gl.glUniformMatrix3fv(Shaders.smsnm.normalmatrixLocation, 1, false,Matrix.normalmatrix(Matrix.multMatrix(Graphics.camera.transformation(),model)), 0);
			gl.glUniformMatrix4fv(Shaders.smsnm.mvpLocation, 1, false,Matrix.multMatrix(viewProjection,model), 0);
			e.model.draw(gl);
		
		}

		model = GLUtils.getTransformationMatrix(new float[]{0.0f,0.0f,0.0f}, new float[]{-1.0f,1.0f,0.0f,0.0f},0.0125f);
		gl.glUniformMatrix3fv(Shaders.smsnm.normalmatrixLocation, 1, false,Matrix.normalmatrix(Matrix.multMatrix(Graphics.camera.transformation(),model)), 0);
		gl.glUniformMatrix4fv(Shaders.smsnm.mvpLocation, 1, false,Matrix.multMatrix(viewProjection,model), 0);

		//Model m = loadedModels.get("hellknight");
		Model hk = loadedModels.get("hellknight");
		hk.draw(gl);

		
		
		model = GLUtils.getTransformationMatrix(new float[]{0.0f,0.0f,0.0f}, new float[]{1.0f,0.0f,0.0f,0.0f},1f);
		gl.glUniformMatrix3fv(Shaders.smsnm.normalmatrixLocation, 1, false,Matrix.normalmatrix(Matrix.multMatrix(Graphics.camera.transformation(),model)), 0);
		gl.glUniformMatrix4fv(Shaders.smsnm.mvpLocation, 1, false,Matrix.multMatrix(viewProjection,model), 0);

		World.regions[0].regionGraphicsMesh.draw(gl);
		
		
		
		
		//We've filled the GBuffer.

		
		
		
		


		/**
		 * For each light render out, with shadows if applicable.
		 */

		//Use the light buffer.
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, lightbufferFbo);
		gl.glDrawBuffers(1, lightbufferBuffers, 0);


		//Group lights based on their type. For now we only support point lights.

		//Bind the appropriate textures so that the chosen illumination shader(s) can do their job.
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[0]);
		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[1]);
		gl.glActiveTexture(GL.GL_TEXTURE2);
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[2]);
		gl.glActiveTexture(GL.GL_TEXTURE3);
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[3]);



		/**
		 * For testing we simply wipe the whole screen over with the light. 
		 * It is VITAL to optimise this - draw the light geometry (cone/sphere, concave, culled one side? Think about how to interact with Z-buffer?)
		 */
		gl.glUseProgram(Shaders.simpleLightShader.id);

		
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);

		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  fsvbo);
		gl.glVertexAttribPointer(Shaders.simpleLightShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		gl.glEnableVertexAttribArray(Shaders.simpleLightShader.vertexLocation);

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		
		

		/**
		 * Post-process effects, overlays, output stage.
		 */

		

		//renderDebugScreen(gl);

	}



	public void renderDebugScreen(GL3 gl){
		gl.glUseProgram(Shaders.simpleTextureShader.id);
		gl.glUniform4fv(Shaders.simpleTextureShader.posdimLocation, 1, new float[]{0.0f,0.0f,1.0f,1.0f}, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[0]);


		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  fsvbo);
		gl.glVertexAttribPointer(Shaders.simpleTextureShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		gl.glEnableVertexAttribArray(Shaders.simpleTextureShader.vertexLocation);

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		//gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);

	}
	public void preloadScreen(GL3 gl){
		gl.glUseProgram(Shaders.simpleTextureShader.id);
		gl.glUniform4fv(Shaders.simpleTextureShader.posdimLocation, 1, new float[]{0.0f,0.0f,1.0f,1.0f}, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, loadTex);


		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  fsvbo);
		gl.glVertexAttribPointer(Shaders.simpleTextureShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		gl.glEnableVertexAttribArray(Shaders.simpleTextureShader.vertexLocation);

		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		//gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		//Log.info("error code2 is "+gl.glGetError());
		
	}


	public void drawOverlays(GL3 gl){

	}




	@Override
	public void dispose(GLAutoDrawable arg0) {

		Log.info("Disposing of OpenGL resources.");
		GL3 gl = arg0.getGL().getGL3();

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
		 * Dispose of anything else we might have forgotten. Also GBuffer...
		 */
		gl.glDeleteFramebuffers(2, new int[]{deferredFbo,lightbufferFbo},0);
		gl.glDeleteTextures(6, new int[]{
				deferredTextures[0],
				deferredTextures[1],
				deferredTextures[2],
				deferredTextures[3], 
				lightTextures[0],
				lightTextures[1]},
				0);

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
		GL3 gl = arg0.getGL().getGL3();
		gl.setSwapInterval(1);
		
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

		
		/**
		 * Load the VBO used when showing textures.
		 */
		fsvbo = Utils.loadFullscreenVBO(gl);
		
		/**
		 * Load the shaders.
		 */
		Shaders.load(gl);

		
		//TODO the below should be done at another time for speed. Get the load screen up quickly.
		/**
		 * Configure the depth tests.
		 */
		//gl.glDepthFunc(GL.GL_LEQUAL);//TODO verify this

		createBuffers(gl);
	}



	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int width, int height) {
		Graphics.camera.width=width;
		Graphics.camera.height=height;

		Graphics.camera.recalculateProjection();

		resizeBuffers(arg0.getGL().getGL3());
		//Resize buffers here.
	}

	private void createBuffers(GL3 gl) {
		int gbufferformat = GL3.GL_RGBA16F;
		int lightBufferFormat = GL3.GL_RGBA16F;
		/**
		 * First we create the deferred buffer and textures.
		 */
		if (!gl.isExtensionAvailable("GL_EXT_framebuffer_object")) {Log.err("FBOs are not supported, cannot create."); return;}


		deferredTextures = new int[4];
		gl.glGenTextures(4,deferredTextures,0);

		/**
		 * 0
		 */
		int fbotex1 = deferredTextures[0];
		gl.glBindTexture(GL.GL_TEXTURE_2D, fbotex1);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);//LINEAR?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);//commenting these kills speed?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_RGBA16, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGBA, GL3.GL_SHORT, null);



		/**
		 * 1,2
		 */
		for(int i = 1; i<3; i++){
			int fbotex = deferredTextures[i];
			gl.glBindTexture(GL.GL_TEXTURE_2D, fbotex);
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);//LINEAR?
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);//commenting these kills speed?
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, gbufferformat, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGBA, GL.GL_FLOAT, null);
		}

		/**
		 * Depth
		 */
		gl.glBindTexture(GL.GL_TEXTURE_2D,  deferredTextures[3]);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);//LINEAR?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);//commenting these kills speed?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

		//TODO CHECKME gl.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_DEPTH_TEXTURE_MODE, GL3.GL_LUMINANCE);//when read as tex interpret as luminance. can be alpha
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_COMPARE_FUNC, GL3.GL_LEQUAL);

		//gl.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_COMPARE_MODE, GL3.GL_COMPARE_R_TO_TEXTURE); //I FUCK SHIT UP WHAT THE FUCK AM I 
		//gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT32 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_DEPTH_COMPONENT/*depth component*/, GL3.GL_UNSIGNED_INT/*UINT/FLOAT?*/, null);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_DEPTH24_STENCIL8 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_DEPTH_STENCIL/*depth component*/, GL3.GL_UNSIGNED_INT_24_8/*UINT/FLOAT?*/, null);



		/**
		 * Now we create the light buffer.
		 */


		//It IS acceptable to share the depth texture between multiple FBOs! That makes stencilling easy.

		lightTextures = new int[2];
		gl.glGenTextures(2,lightTextures,0);


		gl.glBindTexture(GL.GL_TEXTURE_2D, lightTextures[0]);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);//LINEAR?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);//commenting these kills speed?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		//TODO not needing an alpha!

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, lightBufferFormat, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGB, GL.GL_FLOAT, null);



		gl.glBindTexture(GL.GL_TEXTURE_2D, lightTextures[1]);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);//LINEAR?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);//commenting these kills speed?
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		//TODO not needing an alpha!

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, lightBufferFormat, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGB, GL.GL_FLOAT, null);




		/**
		 * We've made the targets, now make and configure the Framebuffers.
		 */
		int[] temp = new int[2];
		gl.glGenFramebuffers(2, temp, 0);

		deferredFbo = temp[0];
		lightbufferFbo=temp[1];



		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, lightbufferFbo);
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, lightTextures[0], 0 );
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT1, GL.GL_TEXTURE_2D, lightTextures[1], 0 );
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_DEPTH_STENCIL_ATTACHMENT, GL.GL_TEXTURE_2D, deferredTextures[3], 0);

		int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);

		boolean fine = false;
		if (status==GL.GL_FRAMEBUFFER_COMPLETE){fine = true;}
		if(!fine){Log.err("Creation of light FBO failed! Error: "+status);}


		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, deferredFbo);
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, deferredTextures[0], 0 );
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT1, GL.GL_TEXTURE_2D, deferredTextures[1], 0 );
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT2, GL.GL_TEXTURE_2D, deferredTextures[2], 0 );
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL3.GL_DEPTH_STENCIL_ATTACHMENT, GL.GL_TEXTURE_2D, deferredTextures[3], 0 );


		status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);

		fine = false;
		if (status==GL.GL_FRAMEBUFFER_COMPLETE){fine = true;}
		if(!fine){Log.err("Creation of deferred target FBO failed! Error "+status);}

		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);


	}





	private void resizeBuffers(GL3 gl) {

		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[0]);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_RGBA16 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGBA/*depth component*/, GL3.GL_SHORT/*UINT/FLOAT?*/, null);

		for(int i = 1; i<3; i++){
			int fbotex = deferredTextures[i];
			gl.glBindTexture(GL.GL_TEXTURE_2D, fbotex);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_RGBA16F, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGBA, GL.GL_FLOAT, null);
		}
		gl.glBindTexture(GL.GL_TEXTURE_2D, deferredTextures[3]);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_DEPTH24_STENCIL8 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_DEPTH_STENCIL/*depth component*/, GL3.GL_UNSIGNED_INT_24_8/*UINT/FLOAT?*/, null);


		gl.glBindTexture(GL.GL_TEXTURE_2D, lightTextures[0]);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_RGB16F, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGB, GL.GL_FLOAT, null);

		gl.glBindTexture(GL.GL_TEXTURE_2D, lightTextures[1]);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_RGB16F, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_RGB, GL.GL_FLOAT, null);

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
			
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);//Default to GL_Nearest for performance. GL_Linear bilerps
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);//Other option is to GL_NEAREST it up and clip to nearest pixel
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

			gl.glGenerateMipmap(GL3.GL_TEXTURE_2D);
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





	public void loadMaterial(Material m2, GL3 gl) {
		if(loadedMaterials.containsValue(m2)) return;
		m2.loadIntoGraphics(gl);
		loadedMaterials.put(m2.matname, m2);
	}



	public void loadModel(Model m, GL3 gl) {
		if(loadedModels.containsValue(m)) return;
		m.loadIntoGraphics(gl);
		loadedModels.put(m.name, m);
	}



	/*
	 * Reduces redundant state calls esp when drawing multiple objects sharing buffers.
	 */

	int lastVbo = -1;
	public void bindBuffer(GL3 gl, int glArrayBuffer, int vboId) {
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
	public void bindTexture(GL3 gl, int glTexture2d, int texId) {
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
				Log.warn("openGL threw an invalid value error.");
				return;
			}
		}
		
	}
	
	
	
	
}
