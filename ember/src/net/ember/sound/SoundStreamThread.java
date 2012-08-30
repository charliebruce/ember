package net.ember.sound;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.openal.AL;

import net.ember.logging.Log;

/**
 * A simple object to process audio streams on a regular basis as required.
 * @author Charlie
 *
 */
public class SoundStreamThread implements Runnable {

	public static final long SLEEPTIME = 20;
	boolean closing = false;

	public static List<Stream> toRemove = new LinkedList<Stream>();
	@Override
	public void run() {
		while(!closing){

			/**
			 * Loop through the active streams
			 */
			//TODO synchronised(streams)? : 
			/*Exception in thread "Sound Streamer Thread" java.util.ConcurrentModificationException
			at java.util.LinkedList$ListItr.checkForComodification(Unknown Source)
			at java.util.LinkedList$ListItr.next(Unknown Source)
			at net.ember.sound.SoundStreamThread.run(SoundStreamThread.java:24)
			after ogg stream complete.
			 */
			for (Stream s: Sound.streams){
				/**
				 * Process the active streams
				 */
				s.process(Sound.al);

				/**
				 * Remove the inactive streams.
				 */
				if(s.canClose(Sound.al)){
					closeStream(s,Sound.al);
				}


			}

			//Prevent a concurrent modification exception.
			for(Stream s: toRemove){
				Sound.streams.remove(s);
				s=null;
			}
			toRemove.clear();

			/**
			 * Avoid hitting the CPU too hard with useless work. 
			 */
			try {
				Thread.sleep(SLEEPTIME);
			} catch (InterruptedException e) {
				Log.info("Interrupted exception in SoundStreamThread.");
			}
		}

		/**
		 * Sound system is closing. We should destroy the streams either here, as Sound expects.
		 */
		for(Stream s: Sound.streams){
			closeStream(s,Sound.al);
		}

	}

	public void close() {
		closing=true;
	}

	private static final void closeStream(Stream s, AL al){
		s.destroy(al);
		toRemove.add(s);
	}
}
