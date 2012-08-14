package net.ember.graphics;

import javax.media.opengl.GL;

import net.ember.logging.Log;

public class FontRenderer {

	
	void render(Font font, String text, float posx, float posy, float pixscale, GL gl){
		if(!font.loaded)
		{
			Log.info("A font has tried to be used before it has been loaded. Potential stall here...");
			font.loadIntoGraphics(gl);
		}
		
		
		
	}
}
