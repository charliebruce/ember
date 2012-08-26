package net.ember.sound;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import net.ember.filesystem.Filesystem;
import net.ember.logging.Log;

import com.jogamp.openal.AL;

/**
 * An OGG Vorbis audio stream.
 * 
 * 
 * TODO change:
 * Allow to read from an URL, saving to disk
 * 
 * TODO implement:
 * Cache first half second of clips, to allow lag-free start.
 * 
 * @author Charlie
 *
 */
public class OggStream implements Stream {

	//Our OGG Decoder object.
	private OggDecoder od;
	
	// The size of a chunk from the stream that we want to read for each update.
	private static int BUFFER_SIZE = 4096*16;

	// The number of buffers used in the audio pipeline.
	private static int NUM_BUFFERS = 2;

	// Buffers hold sound data. There are two of them by default (front/back).
	private int[] buffers = new int[NUM_BUFFERS];

	// Sources are points emitting sound.
	private int[] source = new int[1];

	private int format;	// OpenAL data format
	private int rate;	// sample rate

	boolean streaming=false,canremove=false;

	public OggStream(String filename) throws FileNotFoundException{
		/**
		 * Ogg streams use double buffering.
		 */
			RandomAccessFile f = Filesystem.get(filename);

			od = new OggDecoder(f);
			od.initialize();
			int numChannels = od.numChannels();
			int numBytesPerSample = 2;

			if (numChannels == 1)
				format = AL.AL_FORMAT_MONO16;
			else
				format = AL.AL_FORMAT_STEREO16;

			rate = od.sampleRate();

			// A rough estimation of how much time in milliseconds we can sleep
			// before checking to see if the queued buffers have been played
			// (so that we dont peg the CPU by doing an active wait). We divide
			// by 10 at the end to be safe...
			// round it off to the nearest multiple of 10.
			long sleepTime = (long)(1000.0 * BUFFER_SIZE /
					numBytesPerSample / numChannels / rate / 10.0);
			sleepTime = (sleepTime + 10)/10 * 10;

			//System.err.println("#Buffers: " + NUM_BUFFERS);
			//System.err.println("Buffer size: " + BUFFER_SIZE);
			//System.err.println("Format: 0x" + Integer.toString(format, 16)+" ("+((numChannels==2)?"STEREO":"MONO")+")");
			
			if(sleepTime<SoundStreamThread.SLEEPTIME){
			System.err.println("Sleep time should be at least: " + sleepTime+" but is "+SoundStreamThread.SLEEPTIME+": increase or suffer stuttering audio.");
			}
			// TODO: I am not if this is the right way to fix the endian
			// problems I am having... but this seems to fix it on Linux
			od.setSwap(true);

	}

	public void process(AL al){
		if(streaming){
			if(!update(al)){
				Log.info("OGG Stream complete.");
				al.alSourceStop(source[0]);
				streaming=false;
				canremove=true;
			}else{
			if(!sourcePlaying(al)){
				playback(al);
			}}
		}
	}

	@Override
	public void start(AL al, float[] sourcePos, float[] sourceVel, float[] sourceDir) {

		// Position, Velocity, Direction of the source sound.
		//TODO for now we just play " camera.
		
		al.alGenBuffers(NUM_BUFFERS, buffers, 0); 
		Sound.errorExists();
		
		al.alGenSources(1, source, 0);
		Sound.errorExists();

		al.alSourcefv(source[0], AL.AL_POSITION , sourcePos, 0);
		al.alSourcefv(source[0], AL.AL_VELOCITY , sourceVel, 0);
		al.alSourcefv(source[0], AL.AL_DIRECTION, sourceDir, 0);

    	al.alSourcef(source[0], AL.AL_PITCH, 1.0f);
    	al.alSourcef(source[0], AL.AL_GAIN, 1.0f);
    	
		//al.alSourcef(source[0], AL.AL_ROLLOFF_FACTOR,  10.0f);
		//al.alSourcei(source[0], AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);
		
		//od.dump();
		playback(al);
		streaming=true;
	}

	@Override
	public boolean playing(AL al) {
		return streaming;
	}
	
	private boolean sourcePlaying(AL al){
		int[] state = new int[1];
		al.alGetSourcei(source[0], AL.AL_SOURCE_STATE, state, 0);
		return (state[0] == AL.AL_PLAYING);
	}
	
	/**
	 * Update the stream if necessary
	 */
	public boolean update(AL al) {
		int[] processed = new int[1];
		boolean active = true;

		//Log.debug("update()");
		al.alGetSourcei(source[0], AL.AL_BUFFERS_PROCESSED, processed, 0);

		while (processed[0] > 0)
		{
			int[] buffer = new int[1];

			al.alSourceUnqueueBuffers(source[0], 1, buffer, 0);
			Sound.errorExists();
			//Log.debug("update(): buffer unqueued => " + buffer[0]);

			active = stream(al, buffer[0]);

			//Log.debug("update(): buffer queued => " + buffer[0]);
			al.alSourceQueueBuffers(source[0], 1, buffer, 0); 
			Sound.errorExists();

			processed[0]--;
		}

		return active;
	}
	
	long totalBytes=0;
	/**
	 * Read the next chunk into the given buffer.
	 * @param into
	 */
	public boolean stream(AL al, int into){
		byte[] pcm = new byte[BUFFER_SIZE];
		int    size = 0;
		try {
			if ((size = od.read(pcm)) <= 0){
				return false;
			
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		totalBytes += size;
		//Log.debug("stream(): buffer data => " + into + " totalBytes:" + totalBytes+" and read "+size);

		ByteBuffer data = ByteBuffer.wrap(pcm, 0, size);
		al.alBufferData(into, format, data, size, rate);
		return !Sound.errorExists();
	}
	
	
	public boolean playback(AL al) {
		if (sourcePlaying(al)){
			return true;

		}
			
		//Log.debug("playback(): stream all buffers");
		for (int i = 0; i < NUM_BUFFERS; i++) {
			if (!stream(al, buffers[i]))
				return false;
		}

		//Log.debug("playback(): queue all buffers & play source");
		al.alSourceQueueBuffers(source[0], NUM_BUFFERS, buffers, 0);
		al.alSourcePlay(source[0]);

		return true;
	}
	
	
	/**
	 * Tidy up the stream object completely.
	 */
	public void destroy(AL al) {
		al.alSourceStop(source[0]);

		int[] queued = new int[1];
		al.alGetSourcei(source[0], AL.AL_BUFFERS_QUEUED, queued, 0);

		while (queued[0] > 0)
		{
			int[] buffer = new int[1];
			al.alSourceUnqueueBuffers(source[0], 1, buffer, 0);
			Sound.errorExists();
			queued[0]--;
		}

		od = null;

		for (int i = 0; i < NUM_BUFFERS; i++) {
			al.alDeleteSources(i, source, 0);
			Sound.errorExists();
		}
		
		
	}

	@Override
	public boolean canClose(AL al) {
		return canremove;
	}

	@Override
	public int getSource() {
		return source[0];
	}

	
}
