// xinput.cpp : Defines the exported functions for the DLL application.
//



#include <jni.h>
#include <windows.h>
#include <xinput.h>
#pragma comment(lib, "XInput.lib") // Library containing necessary 360

JNIEXPORT void JNICALL Java_net_ember_input_XInput_getBatteryState
(JNIEnv* env, jclass c, jint index, jobject byteBuffer) {
	
	short *buffer = (short*)(*env)->GetDirectBufferAddress(env, byteBuffer);
	
	XINPUT_BATTERY_INFORMATION xibi;
	
	DWORD result = XInputGetBatteryInformation((int)index, BATTERY_DEVTYPE_GAMEPAD, &xibi);
	
	if (result != ERROR_SUCCESS) {
		buffer[0] = 0;
		return;
	}
	buffer[0]=1;//Success
	buffer[1]=xibi.BatteryLevel;
	buffer[2]=xibi.BatteryType;

}

JNIEXPORT void JNICALL Java_net_ember_input_XInput_getCapabilities
(JNIEnv* env, jclass c, jint index, jobject byteBuffer) {
	
	short *buffer = (short*)(*env)->GetDirectBufferAddress(env, byteBuffer);
	
	XINPUT_CAPABILITIES caps;
	
	DWORD result = XInputGetCapabilities((int)index, BATTERY_DEVTYPE_GAMEPAD, &caps);
	
	if (result != ERROR_SUCCESS) {
		buffer[0] = 0;
		return;
	}
	buffer[0]=1;//Success
	buffer[1]=caps.Flags;
	buffer[2]=caps.Type;
	buffer[3]=caps.SubType;

}

JNIEXPORT void JNICALL Java_net_ember_input_XInput_enable
(JNIEnv* env, jclass c, jboolean enabled) {
	XInputEnable((BOOL)enabled);
}


JNIEXPORT void JNICALL Java_net_ember_input_XInput_poll
(JNIEnv* env, jclass c, jint index, jobject byteBuffer) {
	short *buffer = (short*)(*env)->GetDirectBufferAddress(env, byteBuffer);
	XINPUT_STATE state;
	DWORD result = XInputGetState((int)index, &state);
	if (result != ERROR_SUCCESS) {
		buffer[0] = 0;
		return;
	}
	buffer[0] = 1;
	buffer[1] = state.Gamepad.wButtons;
	buffer[2] = state.Gamepad.bLeftTrigger;
	buffer[3] = state.Gamepad.bRightTrigger;
	buffer[4] = state.Gamepad.sThumbLX;
	buffer[5] = state.Gamepad.sThumbLY;
	buffer[6] = state.Gamepad.sThumbRX;
	buffer[7] = state.Gamepad.sThumbRY;
}

JNIEXPORT void JNICALL Java_net_ember_input_XInput_vibrate(JNIEnv* env, jclass c, jint i, jint li, jint ri){
XINPUT_VIBRATION vibration;
ZeroMemory( &vibration, sizeof(XINPUT_VIBRATION) );
vibration.wLeftMotorSpeed = li; // use any value between 0-65535 here
vibration.wRightMotorSpeed = ri; // use any value between 0-65535 here
XInputSetState( i, &vibration );
}