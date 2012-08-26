package net.ember.data;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

/**
 * A model is a piece of geometry, either static or animated. It could consist of one or more graphical meshes, with materials, and a physics mesh.
 * @author Charlie
 *
 */
public abstract class Model {

	public abstract void draw(GL2 gl);
	public abstract void loadIntoGraphics(GL2 gl);
	public abstract void unloadFromGraphics(GL2 gl);
	public String name;
	public boolean onGraphics=false;
	public float scale=1.0f;
}
