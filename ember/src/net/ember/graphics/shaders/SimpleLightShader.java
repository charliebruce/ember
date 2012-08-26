package net.ember.graphics.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

public class SimpleLightShader extends Shader {

	public int vertexLocation;
	public int posdimLocation;
	private int normalSamplerLocation;
	private int depthSamplerLocation;

	@Override
	public void getLocations(GL2 gl) {
		vertexLocation = gl.glGetAttribLocation(id, "vertexin");
		posdimLocation = gl.glGetAttribLocation(id, "posdim");

		normalSamplerLocation = gl.glGetAttribLocation(id, "normalSampler");
		depthSamplerLocation = gl.glGetAttribLocation(id, "depthSampler");
	}

	@Override
	public void setUniforms(GL2 gl) {

		gl.glUniform1i(normalSamplerLocation, 1);
		gl.glUniform1i(depthSamplerLocation, 3);
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
