package net.ember.input;

import javax.vecmath.Vector3f;

import net.ember.client.Client;
import net.ember.client.Preferences;
import net.ember.game.World;
import net.ember.graphics.Graphics;
import net.ember.logging.Log;

/**
 * A class to handle user input.
 * @author Charlie
 *
 */
public class Input {

	/**
	 * By default we use XInput, but the native library loader may override this.
	 */
	public static boolean useXInput = true;
	public static EmberKeyListener keyListener = new EmberKeyListener();
	private static Gamepad gamepad;
	/**
	 * Set up the inputs.
	 */
	public static void init(){

		if(Preferences.gamepad){
			if(useXInput){
				XInput.enable(true);
				gamepad = new XInputXboxController();
			}
			else{
				gamepad = new JInputXboxController();
			}
			gamepad.init();
		}
	}
	
	/**
	 * Close down any open controllers etc.
	 */
	public static void close(){
		if(Preferences.gamepad){
			if(useXInput)
				XInput.enable(false);
		}
	}
	
	//To save regular object allocations.
	static private Vector3f temp = new Vector3f();
	
	
	/**
	 * Process input events. This should eventually use some key binding system.
	 * For now we use fairly hard-coded stuff.
	 */
	public static void tick(){
		
		/**
		 * Where applicable, handle gamepad input.
		 */
		if(Preferences.gamepad&&gamepad!=null&&gamepad.isConnected()){
			gamepad.poll();
		}
		
		
		
		
		/**
		 * Handle key and mouse input.
		 */
		boolean[] gamepadButtons = gamepad.getButtons();


		if(keyListener.keys[KeyBinding.keyboardQuit]||gamepadButtons[KeyBinding.controllerQuit]){
			Log.info("Quitting.");
			Client.quitUrgent();
		}
		
		
		//TODO OMG OMG Callbacks and event and binding model.
		if(keyListener.keys[KeyBinding.keyboardForward]){
			temp.set(Graphics.camera.getViewDirection());
			temp.normalize();
			temp.scale(0.005f);
			World.player.movement.set(temp);
		}
		
		if(keyListener.keys[KeyBinding.keyboardBackward]){
			temp.set(Graphics.camera.getViewDirection());
			temp.normalize();
			temp.scale(-10f/60f);
			Graphics.camera.position.add(temp);
		}
		if(keyListener.keys[KeyBinding.keyboardRight]){
			temp.set(Graphics.camera.getRight());
			temp.normalize();
			temp.scale(10f/60f);
			Graphics.camera.position.add(temp);
		}
		
		if(keyListener.keys[KeyBinding.keyboardLeft]){
			temp.set(Graphics.camera.getRight());
			temp.normalize();
			temp.scale(-10f/60f);
			Graphics.camera.position.add(temp);
		}
		
		
		
		
		if(keyListener.keys[70])//f
		{	Graphics.camera.setOrientation(Graphics.camera.getAzimuth()+0.05f, Graphics.camera.getElevation());
		Graphics.camera.setOrientation(Graphics.camera.getAzimuth(), Graphics.camera.getElevation()+0.05f);
		}
		//Jump
		if(keyListener.keys[KeyBinding.keyboardJump]||gamepadButtons[KeyBinding.controllerJump])//Jump
		{
			World.player.jump();
		}
		
		
		Graphics.camera.setOrientation(Graphics.camera.getAzimuth()+keyListener.pixx*0.00005f, Graphics.camera.getElevation()+keyListener.pixy*0.00005f);
		
		
		
		/**
		 * Occasionally (about once every 5-10 seconds) we could check to see if 
		 * a controller has been plugged in or removed, and for wireless 
		 * controllers if battery level has changed.
		 */
		ticksSinceLastUpdate++;
		//Check if null or last read was an error.
		if(ticksSinceLastUpdate==60*5){
			ticksSinceLastUpdate=0;
			if(Preferences.gamepad&&gamepad!=null){
				gamepad.checkStatus();
			}
		}
		
		
		
	}
	
	static int ticksSinceLastUpdate = 0;
}
