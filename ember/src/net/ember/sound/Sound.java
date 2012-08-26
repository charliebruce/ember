package net.ember.sound;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCdevice;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;


import net.ember.filesystem.Filesystem;
import net.ember.graphics.Graphics;
import net.ember.logging.Log;

public class Sound {

	private static Thread streamerThread;
	private static SoundStreamThread soundstreamer;
	static boolean enabled = true;
	static AL al;
	private static ALC alc;
	
	static List<Stream> streams;
	private static LinkedList<SoundAttachment> attachments;
	
	static int clapBuffer;
	
	/**
	 * Initialise the sound system - create lists and initialise objects.
	 */
	public static void init() {
		
		if(enabled){
			Log.info("Sound system loading.");
			try{
				ALut.alutInit();
				al = ALFactory.getAL();
				alc=ALFactory.getALC();
			}catch(Exception ex){
				ex.printStackTrace();
				Log.warn("Audio system failed to initialise!");
				enabled = false;
				return;
			}
			ALCdevice device = alc.alcOpenDevice(null);
			//device = alc.alcOpenDevice("DirectSound3D");
			if (device == null) {
				Log.warn("No AL devices found!");
				enabled = false;
				return;
			}
			String deviceSpecifier = alc.alcGetString(device, ALC.ALC_DEVICE_SPECIFIER);
			if (deviceSpecifier == null) {
				Log.warn("Error getting specifier for default OpenAL device!");
				enabled = false;
				return;
			}
			//http://connect.creativelabs.com/openal/OpenAL%20Wiki/Enumeration%20with%20OpenAL%20on%20Windows.aspx
			//TL;DR:
			//Native (nvidia, creative) implementations best
			//Generic Hardware = OK
			//Generic Software: Shit but useable


			String[] cds = alc.alcGetCaptureDeviceSpecifiers();

			for(int i=0;i<cds.length;i++){
				//System.out.println("Capture device: "+cds[i]);
			}
			
			if(!errorExists())
				Log.info("Sound initialised successfully, outputting to "+deviceSpecifier);
				
		}else{
			Log.info("Sound system disabled; Not loading.");
		}
		
		soundstreamer = new SoundStreamThread();
		streamerThread = new Thread(soundstreamer,"Sound Streamer Thread");
		
		streams = new LinkedList<Stream>();
		streamerThread.start();
		
		attachments = new LinkedList<SoundAttachment>();
		
		
		al.alDistanceModel(AL.AL_EXPONENT_DISTANCE);
		al.alSpeedOfSound(330.0f);
		
		try {
			clapBuffer = loadWav(Filesystem.get("test/440hz.wav"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] clap = new int[1];
		al.alGenSources(1, clap,0);
	
		al.alSourcei(clap[0], AL.AL_BUFFER, clapBuffer);
		al.alSourcef(clap[0], AL.AL_PITCH, 1.0f);
		al.alSourcef(clap[0], AL.AL_GAIN, 1.0f);
		al.alSourcefv(clap[0], AL.AL_POSITION, new float[]{0.0f,0.0f,0.0f}, 0);
		al.alSourcefv(clap[0], AL.AL_VELOCITY, new float[]{0.0f,0.0f,0.0f}, 0);
		al.alSourcei(clap[0], AL.AL_LOOPING, AL.AL_TRUE);
		
		//al.alSourcePlay(clap[0]);
		
		
		
		errorExists();
	}
	
	private static int loadWav(RandomAccessFile randomAccessFile) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(randomAccessFile.getFD());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.warn("Unable to load file.");
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

		int[] format = new int[1];
		int[] size = new int[1];
		ByteBuffer[] data = new ByteBuffer[1];
		int[] freq = new int[1];
		int[] loop = new int[1];

		ALut.alutLoadWAVFile(fis, format, data, size, freq, loop);
/*		sb.format = format[0];
		sb.data = data[0];
		sb.size = size[0];
		sb.freq = freq[0];
		sb.loop = loop[0];
*/
		int[]buffers=new int[1];
		al.alGenBuffers(1, buffers, 0);

		
		
		al.alBufferData(buffers[0], format[0], data[0], size[0], freq[0]);

		

		errorExists();
		return buffers[0];
	}

	/**
	 * In a tick event, we update the listeners and position of all sources.
	 */
	public static void tick(){
		updateListener();
		updatePositions();
		errorExists();
	}


	
	/**
	 * We must deallocate our resources, stop our worker thread and close.
	 */
	public static void close() {
		/**
		 * Sound streamer closes all the sound stream objects.
		 */
		soundstreamer.close();
		
		
	}
	/**
	 * Check for an error reported by AL.
	 * @return True if error found.
	 */
	public static boolean errorExists(){
		int a = al.alGetError();
		if(a!=AL.AL_NO_ERROR){
			String warnString = "Audio system detected an error: ";
			if(a==AL.AL_OUT_OF_MEMORY){
				Log.warn(warnString+"Audio system out of memory!");
			}
			if(a==AL.AL_INVALID_OPERATION){
				Log.warn(warnString+"The operation was invalid.");
			}
			if(a==AL.AL_INVALID_VALUE){
				Log.warn(warnString+"The value was invalid.");
			}
			if(a==AL.AL_INVALID_ENUM){
				Log.warn(warnString+"The enum was invalid.");
			}
			if(a==AL.AL_INVALID_NAME){
				Log.warn(warnString+"The name was invalid.");
			}
			if(a==AL.AL_INVALID){
				Log.warn(warnString+"Something else was invalid.");
			}
		}
		return(a!=AL.AL_NO_ERROR);
	}


	public static void playMusicToCompletion(String name) {
		if(name.endsWith(".ogg")){
			try{
				OggStream os = new OggStream(name);
				streams.add(os);
				float[] pos = new float[]{Graphics.camera.position.x,Graphics.camera.position.y,Graphics.camera.position.z};
				float[] vel = new float[]{Graphics.camera.velocity.x,Graphics.camera.velocity.y,Graphics.camera.velocity.z};
				float[] dir = new float[]{1.0f,0.0f,0.0f};
				os.start(al,pos,vel,dir);
				attachments.add(new SoundAttachment(os,Graphics.camera));
			}catch(FileNotFoundException e){
				Log.warn("OGG play failed: Could not find file "+name);
			}
			
			
		}else Log.warn("Unable to play non-ogg.");
	}

	

	static float[] temp = new float[3];
	
	static void updatePositions(){
		for(SoundAttachment a: attachments){
			if(a.stream.playing(al)){
				temp[0]=a.soundAt.position.x;
				temp[1]=a.soundAt.position.y;
				temp[2]=a.soundAt.position.z;
				al.alSourcefv(a.source, AL.AL_POSITION , temp, 0);
				temp[0]=a.soundAt.velocity.x;
				temp[1]=a.soundAt.velocity.y;
				temp[2]=a.soundAt.velocity.z;
				al.alSourcefv(a.source, AL.AL_VELOCITY , temp, 0);
				temp[0]=1.0f;
				temp[1]=0.0f;
				temp[2]=0.0f;
				al.alSourcefv(a.source, AL.AL_DIRECTION, temp, 0);
				//Log.info("Moving source to "+a.soundAt.position.x+","+a.soundAt.position.y+","+a.soundAt.position.z);
			}
			else{
				attachments.remove();//TODO check this behaves as expected.
				a=null;
			}
			
			
		}
	}
	
	
	
	
	static float[] listenerPos = new float[3];
	static float[] listenerVel = new float[3];
	static float[] listenerOri = new float[6];
	
	private static void updateListener() {

		listenerPos[0] = Graphics.camera.position.x;
		listenerPos[1] = Graphics.camera.position.y;
		listenerPos[2] = Graphics.camera.position.z;
		
		Vector3f ld = new Vector3f(Graphics.camera.getViewDirection());
		Vector3f right = new Vector3f(Graphics.camera.getRight());
		
		ld.normalize();
		right.normalize();
		
		//Log.info("Looking "+ld.toString()+ " right "+right.toString());
		
		listenerOri[0]=ld.x;
		listenerOri[1]=ld.y;
		listenerOri[2]=ld.z;
		
		ld.cross(right,ld);
		ld.normalize();
		
		listenerOri[3]=ld.x;
		listenerOri[4]=ld.y;
		listenerOri[5]=ld.z;
		
		listenerVel[0]=Graphics.camera.velocity.x;
		listenerVel[1]=Graphics.camera.velocity.y;
		listenerVel[2]=Graphics.camera.velocity.z;
		
		setListenerValues();
	}

		private static void setListenerValues() {
			al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
			al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
			al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
		}

}
