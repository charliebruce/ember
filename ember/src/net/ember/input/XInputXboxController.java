package net.ember.input;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import net.ember.logging.Log;

/**
 * A gamepad using Microsoft's XInput library via JNI/a DLL.
 * TODO standardise this with other controllers.
 * @author Charlie
 *
 */
public class XInputXboxController implements Gamepad {

	private int index;
	private ByteBuffer bb;
	private ShortBuffer sb;
	
	int battery;
	
	public boolean connected = false;
	public boolean voiceSupported = false;
	public boolean wireless = false;
	
	int buttons=0, prevbuttons=0;
	
	/**
	 * Left and right trigger travel, from 0f to 1f
	 */
	public float lt=0.0f,rt=0.0f;
	
	
	/**
	 * Obtain the latest information from the controller.
	 */
	public void poll(){
		
		XInput.poll(index, bb);
		if(sb.get()!=1){
			Log.warn("Poll of controller " + index + " failed!");
		}
		
		buttons = sb.get();
		//TODO read axes
		lt=sb.get()*XInput.recipMaxTriggerTravel;
		rt=sb.get()*XInput.recipMaxTriggerTravel;
		//lx=sb.get();
		//ly=sb.get();
		//rx=sb.get();
		//ry=sb.get();

		//if((lx*lx+ly*ly)<XInput.XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE_SQUARED){
			//Deadzone says no.
		//}

		bb.clear();
		sb.rewind();
		
	}

	/**
	 * Create an object to wrap around an Xbox controller.
	 */
	@Override
	public void init() {
		
		bb = ByteBuffer.allocateDirect(16);//Maximum needed is 8 shorts = 16 bytes.
		bb.order(ByteOrder.nativeOrder());
		sb = bb.asShortBuffer();
		
		/**
		 * Find a suitable controller ID to use, if any.
		 */
		index=-1;
		while(index<3&&!connected){//While the index loves you, and isn't otherwise connected......?
			index++;
			XInput.getCapabilities(index, bb);
			if(sb.get()==1){
				connected=true;
			}
			else{
				bb.clear();
				sb.rewind();
			}
		}
		
		if(!connected){
			Log.info("No XInput controllers found connected to the system.");
			return;
		}
		
		Log.debug("Using controller index "+index);
		
		/**
		 * Read the controller's capabilities and type.
		 */
		
		int flag = sb.get();
		if((flag&XInput.XINPUT_CAPS_VOICE_SUPPORTED)!=0){
			voiceSupported = true;
		}
		if(sb.get()!=0)Log.info("Unknown type in capabilities - not a gamepad?");
		
		int subtype = sb.get();
		if(subtype!=XInput.XINPUT_DEVSUBTYPE_GAMEPAD) Log.info("Not a standard gamepad, but some other device - drums, wheel?");
		
		bb.clear();
		sb.rewind();
		XInput.getBatteryState(index, bb);
		
		if(sb.get()!=1) Log.warn("Cannot read battery state.");
		battery = sb.get();
		int batterytype = sb.get();
		
		if((battery&XInput.BATTERY_TYPE_WIRED)!=0)
			wireless=false;
		else 
			wireless=true;
		
		if(wireless){
			Log.info("Wireless controller "+index+" has level "+XInput.getBatteryLevel(battery)+" and is a "+XInput.getBatteryType(batterytype)+" battery.");
		}else{
			Log.info("Wired controller "+index+" connected.");
		}
		
		bb.clear();
		sb.rewind();
		
		poll();
		
	}

	@Override
	public void checkStatus() {
		//Log.debug("Checking status.");
		if(wireless&&connected){
			bb.clear();
			sb.rewind();
			XInput.getBatteryState(index, bb);
			
			
			if(sb.get()!=1) Log.warn("Cannot read battery state.");
			int batterylevel = sb.get();
			if(batterylevel!=battery){
				Log.info("Battery level changed.");
				battery=batterylevel;
			}
			int batterytype = sb.get();
			
			bb.clear();
			sb.rewind();
			
		}
		if(!connected){
			//TODO check for connection
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean[] getButtons() {
		return new boolean[]{ 
				((buttons&XInput.BUTTON_A)!=0),
				((buttons&XInput.BUTTON_B)!=0),
				((buttons&XInput.BUTTON_X)!=0),
				((buttons&XInput.BUTTON_Y)!=0),
				((buttons&XInput.BUTTON_LEFT_SHOULDER)!=0),
				((buttons&XInput.BUTTON_RIGHT_SHOULDER)!=0),
				((buttons&XInput.BUTTON_LEFT_THUMB)!=0),
				((buttons&XInput.BUTTON_RIGHT_THUMB)!=0),
				((buttons&XInput.BUTTON_START)!=0),
				((buttons&XInput.BUTTON_BACK)!=0)
		};
	}
}
