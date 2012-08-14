package net.ember.sound;

import net.ember.game.Entity;

public class SoundAttachment {

	public SoundAttachment(Stream os, Entity e) {
		soundAt=e;
		stream = os;
		source = stream.getSource();
	}
	public Entity soundAt;
	public int source;
	public Stream stream;
}
