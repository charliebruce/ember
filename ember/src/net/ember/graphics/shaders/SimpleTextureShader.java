package net.ember.graphics.shaders;

import javax.media.opengl.GL2;

import net.ember.graphics.Render;
import net.ember.logging.Log;

/**
 * Simply passes a texture on to the screen.
 * Uniform "posdim" is position of centre, scale factor.
 * @author Charlie
 *
 */
public class SimpleTextureShader extends Shader {

	public int vertexLocation;
	public int posdimLocation;
	public int textureLocation;
	@Override
	public void getLocations(GL2 gl) {
		
		vertexLocation = gl.glGetAttribLocation(id, "vertexin");		
		posdimLocation = gl.glGetUniformLocation(id, "posdim");
		textureLocation = gl.glGetUniformLocation(id, "tex");

	
	}
	public void setUniforms(GL2 gl){
		gl.glUniform1i(textureLocation, 0);
	}

	@Override
	public String getName() {
		return "SimpleTextureShader";
	}
	@Override
	String getFragmentCodeName() {
		return "shaders/texture.fp";
	}
	@Override
	String getVertexCodeName() {
		return "shaders/texture.vp";
	}

	
	
}
