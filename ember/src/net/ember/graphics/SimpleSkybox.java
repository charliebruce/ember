package net.ember.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.ember.graphics.shaders.Shaders;

/**
 * Simple because we don't support change over time/blending
 * @author Charlie
 *
 */
public class SimpleSkybox {

	public void draw(GL2 gl){

		gl.glUniformMatrix4fv(arg0, arg1, arg2, arg3, arg4)
		ModelView.Load(Camera.GetCameraMatrix(true)); // Here I load the rotation only camera matrix
		glBindTexture(GL.GL_TEXTURE_CUBE_MAP, Textures[2]); // Bind my cubemap texture
		glDisable(GL_CULL_FACE); // disable face culling so the inside of the cube is rendered

		        gl.glUseProgram(Shaders.sss.id);
		        //Send only view-projection - the model is always centred at the player. 
		        //TODO just a simple plane at z=-1 or 1 or whatever, cubemap = vector towards.?.
		        Shaders.SetUniformData("modelview", GL_FLOAT_MAT4, 1, ModelView.GetMatrix()); // Set the modelview uniform
		        Shaders.SetUniformData("projection", GL_FLOAT_MAT4, 1, Perspective.GetMatrix()); // Set the projection uniform
		        SkyBox.Draw(); // Draw my skybox cube

		glEnable(GL_CULL_FACE);
	}
	
	public void load(GL2 gl){

		int[] ids = new int[1];
		gl.glGenTextures(1,ids,0);
		
		
		gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, ids[0]);

		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL.GL_CLAMP_TO_EDGE);

		for(int i = 0; i < 6; i++)
		{
		        ILinfo TextureInfo;
		        LoadImage(CubeMapFaces[i], &TextureInfo); // Wrapper I made for DevIL image library
		                //Posx,Negx,Posy,Negy,PosZ,Negz
		        glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GL_RGB, TextureInfo.Width, TextureInfo.Height, 0, TextureInfo.Format, GL_UNSIGNED_BYTE, TextureInfo.Data);
		}
		gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP);
		
	}
	
	public void unload(GL2 gl){
		
	}
}
