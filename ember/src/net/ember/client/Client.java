package net.ember.client;

import com.jogamp.newt.opengl.GLWindow;

import net.ember.filesystem.Filesystem;
import net.ember.game.World;
import net.ember.graphics.Graphics;
import net.ember.graphics.Renderer;
import net.ember.input.Input;
import net.ember.logging.Log;
import net.ember.logging.Timer;
import net.ember.natives.NativeLoader;
import net.ember.physics.Physics;
import net.ember.sound.Sound;
import net.ember.window.WindowListener;

public class Client {

	/**
	 * Everything is in the world, and the world ticks and controls everything.
	 */
	public static World world;

	
	/**
	 * The main thread checks this every step.
	 */
	static boolean mainThreadRunning = true;
	
	/**
	 * The GL objects.
	 */
	static GLWindow window;
	static WindowListener wl;

	/**
	 * Load Manager
	 */
	public static LoadManager loadManager;
	private static Thread loadManagerThread;
	
	/**
	 * Download Manager
	 */
	public static DownloadManager downloadManager;
	private static Thread downloadManagerThread;
	
	
	
	/**
	 * Target Frame Rate
	 */
	public static final int TARGET_FPS = 60;
	public static final float FRAME_TIME = 1.0f/TARGET_FPS;
	
	
	/**
	 * Initialise the game engine.
	 */
	public static void init() {
		
		/**
		 * Filesystem comes first, since other bits all load data..
		 */
		Filesystem.init();
		
		/**
		 * Load preferences
		 */
		Preferences.load();
		
		/**
		 * Ensure all the required native code is loaded.
		 */
		NativeLoader.loadNativeLibraries();
		
		/**
		 * Start the simple timekeeper.
		 */
		Timer.init();
	
		/**
		 * Now, initialise the subsystems and window.
		 */
		Graphics.renderer = new Renderer();
		window = Graphics.renderer.createWindow();
		
		/**
		 * Load screen shown.
		 */
		window.display();
		//window.swapBuffers();
		
		
		/**
		 * Start the managers.
		 */
		loadManager = new LoadManager();
		loadManagerThread = new Thread(loadManager, "Load Manager");
		loadManagerThread.start();
		
		downloadManager = new DownloadManager();
		downloadManagerThread = new Thread(downloadManager, "Download Manager");
		downloadManagerThread.start();
		
		
		/**
		 * Switch on subsystems.
		 */
		Input.init();
		Sound.init();
		wl=new WindowListener();
		window.addWindowListener(wl);


		window.addKeyListener(Input.keyListener);
		window.addMouseListener(Input.keyListener);
		//window.addMouseListener(client.getGeneralListener());
		
		
		
		
		/**
		 * Set up a world. For debugging this is a simple scene, but we should go to a main menu first.
		 */
		Physics.init();
		World.init();
		
		Sound.playMusicToCompletion("nonfree/Haven Forest.ogg");
		
		
		while(!Client.loadManager.idle()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//To push all the required textures and data to the GPU.
		Graphics.renderer.loadData=true;
		window.display();
		
		
		World.regions[0].activate();
		
		while(!Client.loadManager.idle()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//To push textures.
		Graphics.renderer.loadData=true;
		window.display();
		
		
		World.createPlayer();
		Graphics.renderer.initialLoadScreen=false;
		Log.info("Initialised the engine.");
	}
	
	
	
	/**
	 * Begin running the game loop.
	 */
	public static void run() {
		//Temporary storage for timekeeping
		long t0,t1,t2,t3,t4,t5;
		while(mainThreadRunning){

			t0 = System.nanoTime();
			/**
			 * First we send the data to the graphics pipeline.
			 */
			window.display();
			t1 = System.nanoTime();
			Timer.event(Timer.Display, t1-t0);
			/**
			 * While the graphics card is processing, we set up the next frame of data.
			 * There should and could be some threading, since tasks like audio update 
			 * might take some time but is independent of the rest of the code, assuming
			 * that the positions of entities have already been applied via physics
			 */
			
			/**
			 * Accept user input.
			 */
			Input.tick();
			t2 = System.nanoTime();
			Timer.event(Timer.Input, t2-t1);
			
			/**
			 * Process the physics simulation forward a frame.
			 */
			Physics.tick();
			t3 = System.nanoTime();
			Timer.event(Timer.Physics, t3-t2);
			
			/**
			 * Process AI and gameplay events
			 */
			World.tick();
			t4 = System.nanoTime();
			Timer.event(Timer.World, t4-t3);
			
			/**
			 * Process sound position updates.
			 */
			Sound.tick();
			t5 = System.nanoTime();
			Timer.event(Timer.Sound, t5-t4);
			
			/**
			 * Ensure that background caching of sound, graphics and download data is going OK 
			 */
			//TODO this
			
			/**
			 * Now we switch the buffers to make everything visible. This stalls the thread 
			 * for the remaining time when double-buffered/vsynced. 
			 */
			//window.swapBuffers();
			//long t2 = System.nanoTime()-t0;
			//Log.debug("time to sync "+t1);
			//System.out.println("Total time "+(System.nanoTime()-t0));
			
			
			
			
			if(((t5-t0)/1000000)>18){
				Log.warn("Frame sync took longer than expected. Breakdown: "+Timer.breakdownCurrentFrame());
			}
			
			Timer.newframe();
			
			//This creates massive judder, setting the title seems to take a while.
			//window.setTitle("Ember: "+ frametime+"ms: Physics "+(phystime-ppt)/1000000+"."+((phystime-ppt)/100000)%10+"ms World "+(worldtime-phystime)/1000000+"."+ ((worldtime-phystime)/100000)%10+"ms Sync "+(t2)/1000000);
		}
		
	}
	
	/**
	 * The main loop is no longer running so we should deallocate resources, save and quit.
	 */
	public static void shutdown(){
		loadManager.stop();
		downloadManager.stop();
		Sound.close();
		Input.close();
		window.setVisible(false);
		/*causes dispose in renderer*/
		window.destroy();
		Log.close();
		Timer.close();
	}
	
	
	/**
	 * Immediately terminate the main game loop. 
	 */
	public static void quitUrgent(){
		mainThreadRunning = false;
	}
	
	/**
	 * Safer shutdown of the engine.
	 */
	public static void quitNonUrgent(){
		//TODO save the game and states first.
		mainThreadRunning = false;
	}

}
