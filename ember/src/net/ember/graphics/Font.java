package net.ember.graphics;

import javax.media.opengl.GL;

public class Font {

	public boolean loaded=false;
	int textureid;
	
	public void loadIntoGraphics(GL gl) {
		// TODO Auto-generated method stub
		int[] ids = new int[1];
		gl.glGenTextures(1, ids, 0);
		textureid=ids[0];
		
		Graphics.renderer.loadTextureData(filename, textureid, gl);
		
		
	}
	
}
