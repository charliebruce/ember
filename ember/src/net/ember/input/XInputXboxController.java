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
	
	int batterylevel;
	int batterytype;
	
	public boolean connected = false;
	public boolean voiceSupported = false;
	public boolean wireless = false;
	
	int buttons=0, prevbuttons=0;
	
	/**
	 * Left and right trigger travel, from 0f to 1f
	 */
	public float lt=0.0f,rt=0.0f;
	
	/**
	 * Left and right sticks, from -1f to 1f
	 */
	float lx,ly,rx,ry;
	
	/**
	 * Left and right sticks as theta and magnitude 0f-1f, radians, 0 is right (think polar coords, x-direction, etc), positive anticlockwise.
	 */
	float ltheta, rtheta,lmag,rmag;
	
	/**
	 * Raw data, range -32768<->32768
	 */
	public short[] axes = new short[4];
	
	/**
	 * Obtain the latest information from the controller.
	 */
	public void poll(){
		
		XInput.poll(index, bb);
		if(sb.get()!=1){
			Log.warn("Poll of controller " + index + " failed!");
		}
		
		buttons = sb.get();
		
		//Scale to 0f...1f
		lt=sb.get()*XInput.recipMaxTriggerTravel;
		rt=sb.get()*XInput.recipMaxTriggerTravel;
		
		//raw
		axes[0]=sb.get(); //Left stick, left-right -32768->32768.
		axes[1]=sb.get();
		axes[2]=sb.get();
		axes[3]=sb.get();

		int distl = (axes[0]*axes[0]+axes[1]*axes[1]);
		if(distl<XInput.XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE_SQUARED){
			//In the deadzone
			lx=0f;ly=0f;
			ltheta=0f;
			lmag=0f;
		}else{
			
			//axes[0] = right, axes[1] = up
			//Scale "travel between limit and deadzone beginning" to 0,1
			lmag = (float) ((Math.sqrt(distl)-XInput.XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE)/XInput.XINPUT_GAMEPAD_LEFT_THUMB_LIMIT_MINUS_DEADZONE);
			
			ltheta = (float) Math.atan2(axes[1], axes[0]);//TODO check this or hilarity (loss of control) may ensue. Drunken mode(cheat)?
			
			lx=(float) (Math.cos(ltheta)*lmag);
			ly=(float) (Math.sin(ltheta)*lmag);
			
		}
		
		int distr = (axes[2]*axes[2]+axes[3]*axes[3]);
		if(distr<XInput.XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE_SQUARED){
			//In the deadzone
			rx=0f;ry=0f;
			rtheta=0f;
			rmag=0f;
		}else{
			
			//axes[0] = right, axes[1] = up
			//Scale "travel between limit and deadzone beginning" to 0,1
			rmag = (float) ((Math.sqrt(distr)-XInput.XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE)/XInput.XINPUT_GAMEPAD_RIGHT_THUMB_LIMIT_MINUS_DEADZONE);
			
			rtheta = (float) Math.atan2(axes[3], axes[2]);//TODO check this or hilarity (loss of control) may ensue. Drunken mode(cheat)?
			
			rx=(float) (Math.cos(rtheta)*rmag);
			ry=(float) (Math.sin(rtheta)*rmag);
			
		}

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
		if(subtype!=XInput.XINPUT_DEVSUBTYPE_GAMEPAD) 
			Log.info("Using a "+XInput.getDeviceType(subtype)+" - only gamepads are supported.");
		
		bb.clear();
		sb.rewind();
		XInput.getBatteryState(index, bb);
		
		if(sb.get()!=1) Log.warn("Cannot read battery state.");
		batterylevel = sb.get();
		batterytype = sb.get();
		
		if((batterylevel&XInput.BATTERY_TYPE_WIRED)!=0)
			wireless=false;
		else 
			wireless=true;
		
		if(wireless){
			Log.info("Wireless controller "+index+" has level "+XInput.getBatteryLevel(batterylevel)+" and is a "+XInput.getBatteryType(batterytype)+" battery.");
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
			int blevel = sb.get();
			if(blevel!=batterylevel){
				Log.info("Battery level changed to "+XInput.getBatteryLevel(blevel));
				batterylevel=blevel;
			}
			int btype = sb.get();
			if(btype!=batterytype){
				Log.info("Battery type changed to "+XInput.getBatteryType(btype));
				batterytype=btype;
			}
			
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

	//TODO smoother. Account for the deadzone. Use LX,LY
	@Override
	public float getForward() {
		return ly;
		//float travel = XInput.recipMaxTravel*axes[1];
		//if(travel*travel < 0.05)
		//return 0;
		//else return travel;
	}

	@Override
	public float getRight() {
		return lx;
		//float travel = XInput.recipMaxTravel*axes[0];
		//if(travel*travel < 0.05)
		//return 0;
		//else return travel;
	}

	@Override
	public float getLookRight() {
		return rx;
		//float travel = XInput.recipMaxTravel*axes[2];
		//if(travel*travel < 0.05)
		//return 0;
		//else return travel;
	}

	@Override
	public float getLookUp() {
		return ry;
		//float travel = XInput.recipMaxTravel*axes[3];
		//if(travel*travel < 0.05)
		//return 0;
		//else return travel;
	}
}
