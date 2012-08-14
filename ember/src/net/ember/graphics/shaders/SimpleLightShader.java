package net.ember.graphics.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

public class SimpleLightShader extends Shader {

	public int vertexLocation;
	public int posdimLocation;

	@Override
	public void getLocations(GL3 gl) {
		vertexLocation = gl.glGetAttribLocation(id, "vertexin");
		posdimLocation = gl.glGetAttribLocation(id, "posdim");
	}

	@Override
	public void setUniforms(GL3 gl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String getFragmentCodeName() {
		return "shaders/simplelight.fp";
	}

	@Override
	String getVertexCodeName() {
		return "shaders/simplelight.vp";
	}

	@Override
	public String getName() {
		return "SimpleLightShader";
	}

}
