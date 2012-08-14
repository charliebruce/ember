package net.ember.sound;

import com.jogamp.openal.AL;

public interface Stream {


	public void start(AL al, float[] pos, float[] vel, float[] dir);
	public void process(AL al);
	public boolean playing(AL al);
	public void destroy(AL al);
	public boolean canClose(AL al);
	public int getSource();
}
