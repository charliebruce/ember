package net.ember.input;

import java.nio.ByteBuffer;

public class XInput {

	static final int BUTTON_DPAD_UP = 0x1;
	static final int BUTTON_DPAD_DOWN = 0x2;
	static final int BUTTON_DPAD_LEFT = 0x4;
	static final int BUTTON_DPAD_RIGHT = 0x8;
	static final int BUTTON_START = 0x10;
	static final int BUTTON_BACK = 0x20;
	static final int BUTTON_LEFT_THUMB = 0x40;
	static final int BUTTON_RIGHT_THUMB = 0x80;
	static final int BUTTON_LEFT_SHOULDER = 0x100;
	static final int BUTTON_RIGHT_SHOULDER = 0x200;
	static final int BUTTON_A = 0x1000;
	static final int BUTTON_B = 0x2000;
	static final int BUTTON_X = 0x4000;
	static final int BUTTON_Y = 0x8000;

	static final int BATTERY_TYPE_DISCONNECTED = 0x00;  // This device is not connected
	static final int BATTERY_TYPE_WIRED = 0x01;  // Wired device, no battery
	static final int BATTERY_TYPE_ALKALINE = 0x02;  // Alkaline battery source
	static final int BATTERY_TYPE_NIMH = 0x03;  // Nickel Metal Hydride battery source
	static final int BATTERY_TYPE_UNKNOWN = 0xFF;  // Cannot determine the battery type

	// These are only valid for wireless, connected devices, with known battery types
	// The amount of use time remaining depends on the type of device.
	static final int BATTERY_LEVEL_EMPTY = 0x00;
	static final int BATTERY_LEVEL_LOW = 0x01;
	static final int BATTERY_LEVEL_MEDIUM = 0x02;
	static final int BATTERY_LEVEL_FULL = 0x03;

	//Capabilities 
	static final int XINPUT_CAPS_VOICE_SUPPORTED = 0x0004;


	//Device Types
	static final int XINPUT_DEVSUBTYPE_GAMEPAD = 0x01;
	static final int XINPUT_DEVSUBTYPE_WHEEL = 0x02;
	static final int XINPUT_DEVSUBTYPE_ARCADE_STICK = 0x03;
	static final int XINPUT_DEVSUBTYPE_FLIGHT_SICK = 0x04;
	static final int XINPUT_DEVSUBTYPE_DANCE_PAD = 0x05;
	static final int XINPUT_DEVSUBTYPE_GUITAR = 0x06;
	static final int XINPUT_DEVSUBTYPE_DRUM_KIT = 0x08;

	/*
	 * Deadzones as suggested by Microsoft 
	 */
	static final int XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE = 7849;
	static final int XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE = 8689;
	static final int XINPUT_GAMEPAD_TRIGGER_THRESHOLD = 30;

	
	/**
	 * Added by Charlie to supplement XInput.
	 */
	static final int XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE_SQUARED = 61606801;
	static final int XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE_SQUARED = 75498721;
	static final int XINPUT_GAMEPAD_LEFT_THUMB_LIMIT_MINUS_DEADZONE = 32768-XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE;
	static final int XINPUT_GAMEPAD_RIGHT_THUMB_LIMIT_MINUS_DEADZONE = 32768-XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE;
	static final float recipMaxTriggerTravel = 1.0f/255.0f;
	static final float recipMaxTravel = 1.0f/32768.0f;
	private static int XINPUT_DEVSUBTYPE_FLIGHT_STICK;

	/**
	 * Return the battery state of the given controller.
	 * ByteBuffer should be at least 3 bits long. 
	 * 
	 * Results are:
	 * bb[0]=1 for success.
	 * bb[1]=BatteryLevel
	 * bb[2]=BatteryType
	 * @param index
	 * @param bb
	 */
	static native void getBatteryState(int index, ByteBuffer bb);

	/**
	 * Return the capabilities of the given controller.
	 * ByteBuffer should be at least 4 bits long.
	 * 
	 * Results are:
	 * bb[0]=1 for success.
	 * bb[1]=Flags
	 * bb[2]=Type
	 * bb[3]=SubType
	 */
	static native void getCapabilities(int index, ByteBuffer bb);

	/**
	 * Enable the XInput library.
	 * @param enable
	 */
	static native void enable(boolean enable);

	/**
	 * Poll the given controller to get its state.
	 * ByteBuffer should be at least 8 bits long.
	 * 
	 * Results are:
	 * bb[0]=1 for success.
	 * bb[1]=Buttons
	 * bb[2]=LeftTrigger
	 * bb[3]=RightTrigger
	 * bb[4]=ThumbLX
	 * bb[5]=ThumbLY
	 * bb[6]=ThumbRX
	 * bb[7]=ThumbRY
	 * 
	 * @param index
	 * @param bb
	 */
	static native void poll(int index, ByteBuffer bb);

	/**
	 * Set the vibration state of the given controller.
	 * 
	 * Accepts 0-65535 with 0 being off.
	 * @param index
	 * @param left
	 * @param right
	 */
	static native void vibrate(int index,int left, int right);
	
	
	public static String getBatteryType(int batterytype){
		if(batterytype==BATTERY_TYPE_ALKALINE) return "Alkaline";
		if(batterytype==BATTERY_TYPE_NIMH) return "Ni-MH";
		if(batterytype==BATTERY_TYPE_WIRED) return "Wired";
		if(batterytype==BATTERY_TYPE_DISCONNECTED) return "Disconnected";
		
		return "Unknown";
	}
	
	public static String getDeviceType(int devtype){
		if(devtype==XINPUT_DEVSUBTYPE_GAMEPAD) return "Gamepad";
		if(devtype==XINPUT_DEVSUBTYPE_WHEEL) return "Wheel";
		if(devtype==XINPUT_DEVSUBTYPE_ARCADE_STICK) return "Arcade Stick";
		if(devtype==XINPUT_DEVSUBTYPE_FLIGHT_STICK) return "Flight Stick";
		if(devtype==XINPUT_DEVSUBTYPE_DANCE_PAD) return "Dance Pad";
		if(devtype==XINPUT_DEVSUBTYPE_GUITAR) return "Guitar";
		if(devtype==XINPUT_DEVSUBTYPE_DRUM_KIT) return "Drum Kit";		
		
		return "Unknown";
	}

	public static String getBatteryLevel(int batterylevel) {
		if(batterylevel==BATTERY_LEVEL_EMPTY) return "Empty";
		if(batterylevel==BATTERY_LEVEL_LOW) return "Low";
		if(batterylevel==BATTERY_LEVEL_MEDIUM) return "Medium";
		if(batterylevel==BATTERY_LEVEL_FULL) return "Full";
		
		return "Unknown";
	}

}
