package net.ember.natives;

import java.io.File;

import net.ember.filesystem.Filesystem;
import net.ember.input.Input;
import net.ember.logging.Log;

public class NativeLoader {

	/**
	 * Load all of the native code required.
	 */
	public static void loadNativeLibraries() {
		//Find OS and choose the native folders appropriately.
		String osname = System.getProperty("os.name");
		String osarch = System.getProperty("os.arch");

		Log.info("Platform: Running "+osname+" "+osarch+", version "+System.getProperty("os.version")+".");
		String os = "osx";
		String ext = ".dll";
		if (osname.toLowerCase().contains("linux")||osname.toLowerCase().contains("unix")) {os="linux";ext=".so";}
		if (osname.toLowerCase().contains("windows")) {os="win"; ext=".dll";}
		String arch = "x86";
		if(osarch.contains("64")) arch="x64";





		//Common Libraries required
		String[] libs = new String[]{"gluegen-rt","joal","nativewindow_awt","nativewindow_win32","newt","jinput-raw","jinput-dx8","jogl_desktop","OpenAL32"};

		//Jinput fix
		if(arch.equals("x64")){
			libs[5]=libs[5]+"_64";//TODO also include jinput-dx8_64
			libs[6]=libs[6]+"_64";
		}
		//jinput has odd naming, check for linux especially


		//Load them.
		for(int i = 0; i<libs.length; i++){
			File f = Filesystem.getOnDisk("lib"+File.separator+os+"-"+arch+File.separator+libs[i]+ext);
			System.load(f.getAbsolutePath());
		}



		//Windows-specific binaries


		//XInput
		if(os.equals("win")){
			File f = Filesystem.getOnDisk("lib/win-"+arch+"/xinput.dll");
			if(f.exists()){
				System.load(f.getAbsolutePath());
			}
			else{
				Input.useXInput = false;
			}
		}else{
			//We cannot use XInput library on non-Windows platforms. No rumble for Mac/Linux :(
			Input.useXInput = false;
		}
		
	}

}
