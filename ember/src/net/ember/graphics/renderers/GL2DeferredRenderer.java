package net.ember.graphics.renderers;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import net.ember.data.Model;
import net.ember.game.Entity;
import net.ember.game.World;
import net.ember.graphics.GLUtils;
import net.ember.graphics.Graphics;
import net.ember.graphics.Render;
import net.ember.graphics.Utils;
import net.ember.graphics.shaders.Shaders;
import net.ember.logging.Log;
import net.ember.math.Matrix;

/**
 * Deferred (pre-pass) rendering specific code.
 * @author Charlie
 *
 */
public class GL2DeferredRenderer implements GL2ForwardRenderer {

	/**
	 * The textures to which the deferred buffers are written.
	 */
	public static int[] deferredTextures;
	static int[] lightTextures;
	


	/**
	 * The framebuffers used in the deferred pipeline.
	 */
	static int lightbufferFbo;
	static int deferredFbo;

	
	/**
	 * Some constants for multi-render targets.
	 */
	final static int[] gbufferBuffers = {GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1, GL3.GL_COLOR_ATTACHMENT2};
	final static int[] lightbufferBuffers = {GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1};




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
	public static void deferredPipeline(GL2 gl){

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
		Model hk = Render.loadedModels.get("hellknight");
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

		
		//gl.glBindBuffer(GL.GL_ARRAY_BUFFER,  Render.fsvbo);
		//gl.glVertexAttribPointer(Shaders.simpleLightShader.vertexLocation, 2, GL.GL_FLOAT, false, 0,0);//2=xy,0,0/no stride, no offset
		//gl.glEnableVertexAttribArray(Shaders.simpleLightShader.vertexLocation);

		//gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
		
		

		/**
		 * Post-process effects, overlays, output stage.
		 */

		

		//renderDebugScreen(gl);

	}

	
	
	
	public static void createBuffers(GL2 gl) {
		int gbufferformat = GL2.GL_RGBA16F;
		int lightBufferFormat = GL2.GL_RGBA16F;
		/**
		 * First we create the deferred buffer and textures.
		 */
		if (!gl.isExtensionAvailable("GL_EXT_framebuffer_object")) {Log.err("FBOs are not supported, cannot create."); return;}
		//On GL2 test, GL_FRAMEBUFFER_UNSUPPORTED_EXT thrown but this is supported.

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
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL3.GL_LEQUAL);

		//gl.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_COMPARE_MODE, GL3.GL_COMPARE_R_TO_TEXTURE); //I FUCK SHIT UP WHAT THE FUCK AM I 
		//gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL3.GL_DEPTH_COMPONENT32 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_DEPTH_COMPONENT/*depth component*/, GL3.GL_UNSIGNED_INT/*UINT/FLOAT?*/, null);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL2.GL_DEPTH24_STENCIL8 /*GL3.GL_DEPTH_COMPONENT32F*/, Graphics.camera.width, Graphics.camera.height, 0, GL3.GL_DEPTH_STENCIL/*depth component*/, GL3.GL_UNSIGNED_INT_24_8/*UINT/FLOAT?*/, null);



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
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL2.GL_DEPTH_STENCIL_ATTACHMENT, GL.GL_TEXTURE_2D, deferredTextures[3], 0 );


		status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);

		fine = false;
		if (status==GL.GL_FRAMEBUFFER_COMPLETE){fine = true;}
		if(!fine){Log.err("Creation of deferred target FBO failed! Error "+status);}
		//36061 = GL_FRAMEBUFFER_UNSUPPORTED_EXT
		
		
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);


	}





	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		GL2 gl = drawable.getGL().getGL2();
		
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





	public static void unload(GL2 gl) {
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
	}


}
