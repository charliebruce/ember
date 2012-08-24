package net.ember.graphics.shaders;

import javax.media.opengl.GL3;

/**
 * A shader, which implements Blinn-Phong for a point light with no shadows.
 * Attenuation is defined as TODO THIS
 * @author Charlie
 *
 */
public class PointLightShader extends Shader {

	@Override
	public void getLocations(GL3 gl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Point Light Shader";
	}

	@Override
	String getFragmentCodeName() {
		return "shaders/pointlight.fp";
	}

	@Override
	String getVertexCodeName() {
		return "shaders/light.vp";
	}

	@Override
	public void setUniforms(GL3 gl) {
		// TODO Auto-generated method stub
		
	}

}
