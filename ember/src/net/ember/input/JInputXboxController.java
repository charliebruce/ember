package net.ember.input;

import net.ember.logging.Log;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputXboxController implements Gamepad {

	private int id;
	private Controller c;
	boolean connected = false;
	
	@Override
	public void poll() {
		if(connected){
			//TODO implement
		}
	}

	@Override
	public void init() {
		
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] cs = ce.getControllers();

		id=-1;
		for(int i=(cs.length-1);i>-1;i--){
			Log.info("Found "+c.getName());
			if(cs[i].getName().toLowerCase().contains("xbox")){
				id=i;
				Log.debug("Found an Xbox controller, id is "+id);
				connected=true;
			}
		}
		
		if(id==-1){
			Log.info("No JInput controllers found.");
			connected=false;
		}
		
		c = cs[id];
		
		poll();

	}

	@Override
	public void checkStatus() {
		//TODO detect connection/disconnection here.
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean[] getButtons() {
		// TODO Auto-generated method stub
		//A,B,X,Y,LB,RB,LStick,RStick,Start, Back//HAT
		return new boolean[]{false,false,false,false,false,false,false,false};
	}

}
