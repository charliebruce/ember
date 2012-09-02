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
	public static EmberKeyListener keyListener = new EmberKeyListener();
	private static Gamepad gamepad;
	
	
	/**
	 * Set up the inputs.
	 */
	public static void init(){

		//TODO: Set it up differently. Gamepad and NonGamepad Interfaces, applied by client instead. 
		if(Preferences.gamepad){
			if(Preferences.useXInput){
				XInput.enable(true);
				gamepad = new XInputXboxController();
				//((XInputXboxController) gamepad).rumbleOn();
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
			if(Preferences.useXInput)
				XInput.enable(false);
		}
	}
	
	//To save regular object allocations.
	static private Vector3f temp = new Vector3f();
	static private Vector3f temp1 = new Vector3f();
	static private Vector3f temp2 = new Vector3f();
	
	
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
		
		if(Preferences.useXInput){
			if(gamepad.getLeftTrigger()>0.5f){
				((XInputXboxController)gamepad).rumbleOn();
			}
			else {
				gamepad.rumbleOff();
			}
		}
		
		
		/**
		 * Handle key and mouse input.
		 */
		boolean[] gamepadButtons = gamepad.getButtons();


		if(keyListener.keys[KeyBinding.keyboardQuit]||gamepadButtons[KeyBinding.controllerQuit]){
			Log.info("Quitting due to key press.");
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
		
		/**
		 * If they aren't using the keyboard we also poll the joystick.
		 */
		if(!keyListener.keys[KeyBinding.keyboardForward]&&!keyListener.keys[KeyBinding.keyboardBackward]&&!keyListener.keys[KeyBinding.keyboardRight]&&!keyListener.keys[KeyBinding.keyboardLeft]){
			
			
			//Find the horizontal component of the camera's view direction.
			temp.set(Graphics.camera.getViewDirection());
			temp.y=0.0f;
			temp.normalize();
			
			//Find out which way is right.
			temp1.set(0.0f,1.0f,0.0f);
			temp1.cross(temp1, temp); 
			
			
			float fwd = gamepad.getForward();
			float right = gamepad.getRight();
			
			temp.scale(fwd*0.02f);
			temp1.scale(right*-0.02f); //-ve because FUCK YOU that's why. Seriously. The gamepad gets it right, is my entire world mirrored or is the cross product wrong?
			
			temp2.add(temp, temp1);
			
			World.player.movement.set(temp2);
			
			
			float azi = Graphics.camera.getAzimuth() + gamepad.getLookRight() * 0.05f;
			float ele = Graphics.camera.getElevation() + gamepad.getLookUp() * 0.05f;
			
			//These are already hard limits in the camera code. This should just be used to make angle changing smoother - base it such that dElevation = k dDistanceToEdge
			final float MIN_ELE = 1.4f;
			final float MAX_ELE = 3.14f;
			if(ele<MIN_ELE) ele = MIN_ELE;//Lock to horizontal or above
			if(ele>MAX_ELE) ele = MAX_ELE; //Lock to just below vertical
			Graphics.camera.setOrientation(azi, ele);
			
			
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
