package net.ember.data;

import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import net.ember.client.Client;
import net.ember.graphics.Graphics;
import net.ember.graphics.Material;
import net.ember.graphics.Renderer;
import net.ember.graphics.shaders.Shaders;
import net.ember.logging.Log;

/**
 * A model containing one or more meshes.
 * @author Peter
 *
 */
public class OrderedStaticModel extends Model {
	
	private int vboId;
	//private int indId;
	private Material[] mats;
	float[] data;
	int[] offsets;
	int[] lengths;
	String[] matnames;

	@Override
	public void draw(GL2 gl) {
		
		/*
		if(m.useAlbedoMap())
		{
			Log.info("~Using  alb");
			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, m.ids[0]);
		}
		if(m.useNormalMap())
		{
			Log.info("Bind 1 mids0 is "+m.ids[0]+","+m.ids[1]);
			gl.glActiveTexture(GL2.GL_TEXTURE1);
			gl.glBindTexture(GL.GL_TEXTURE_2D,3);
		}*/
		
		
		Graphics.renderer.bindBuffer(gl, GL.GL_ARRAY_BUFFER,vboId);
		gl.glVertexAttribPointer(Shaders.smsnm.vertexLocation, 3, GL.GL_FLOAT, false, 12*4,0);//xyz
		gl.glEnableVertexAttribArray(Shaders.smsnm.vertexLocation);
		gl.glVertexAttribPointer(Shaders.smsnm.texcoordLocation, 2, GL.GL_FLOAT, false, 12*4,3*4);//st
		gl.glEnableVertexAttribArray(Shaders.smsnm.texcoordLocation);
		gl.glVertexAttribPointer(Shaders.smsnm.normalLocation, 3, GL.GL_FLOAT, false, 12*4,5*4);//nxyz
		gl.glEnableVertexAttribArray(Shaders.smsnm.normalLocation);
		gl.glVertexAttribPointer(Shaders.smsnm.tangentLocation, 4, GL.GL_FLOAT, false, 12*4,8*4);//4=tanxyzw
		gl.glEnableVertexAttribArray(Shaders.smsnm.tangentLocation);//TODO check this?
		
		
		for(int i=0;i<offsets.length;i++){//offsets.length
		//Log.debug("Drawing with "+mats[i].getName()+"," +mats[i].albedoMap+"," +mats[i].ids[0]);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		Graphics.renderer.bindTexture(gl, GL.GL_TEXTURE_2D, mats[i].ids[0]);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		Graphics.renderer.bindTexture(gl, GL.GL_TEXTURE_2D, mats[i].ids[1]);
		
		
		//TODO 128
		//Draw elements - draw N elements from the array starting at offset O
		//gl.glDrawElements(GL2.GL_TRIANGLES, 128, GL2.GL_UNSIGNED_INT, 0);
		gl.glDrawArrays(GL2.GL_TRIANGLES, offsets[i], lengths[i]);

				
			//TODO this also material.
		}
		
	}

	@Override
	public void loadIntoGraphics(GL2 gl) {
		mats=new Material[matnames.length];
		
		for (int i=0;i<matnames.length;i++){
			mats[i] = Graphics.renderer.getMaterial(matnames[i], gl);
			if(mats[i]==null)
				Log.warn("Static model has a null material. This will be a disaster. We could point this towards a null material instead.");
			
		}
		
		int[] vbo = new int[1];
		gl.glGenBuffers(1, vbo, 0);
		vboId = vbo[0];
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER,vboId);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, 4*data.length/*4 bytes per float*/,FloatBuffer.wrap(data), GL.GL_STATIC_DRAW);
		//gl.glBindBuffer(GL.GL_ARRAY_BUFFER,indId);
		//gl.glBufferData(GL.GL_ARRAY_BUFFER, 4*indices.length/*4 bytes per float*/,IntBuffer.wrap(indices), GL.GL_STATIC_DRAW);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
		
		gl.glEnable(GL.GL_ARRAY_BUFFER);//TODO check this?
		Log.info("Load of OderedStaticModel");
		Renderer.assertNoError(gl);
		onGraphics=true;
	}

	@Override
	public void unloadFromGraphics(GL2 gl) {
		gl.glDeleteBuffers(1, new int[]{vboId},0);
	}

	

}
