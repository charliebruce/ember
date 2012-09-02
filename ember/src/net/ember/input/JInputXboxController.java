package net.ember.input;

import net.ember.logging.Log;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputXboxController implements Gamepad {

	private int id;
	private Controller controller;
	boolean connected = false;
	boolean[] buttons = new boolean[8];
	
	/**
	 * Left and right triggers, as of the last poll.
	 */
	float lt = 0f; float rt = 0f;
	
	@Override
	public void poll() {
		if(connected){
			//TODO implement error handling
			if(!controller.poll())
				{
				Log.warn("Failed to poll JInput Xbox controller!");
				connected = false;
				}
			
			
			
			buttons[0] = controller.getComponent(Identifier.Button._0).getPollData()!=0f;
			buttons[1] = controller.getComponent(Identifier.Button._1).getPollData()!=0f;
			buttons[2] = controller.getComponent(Identifier.Button._2).getPollData()!=0f;
			buttons[3] = controller.getComponent(Identifier.Button._3).getPollData()!=0f;
			buttons[4] = controller.getComponent(Identifier.Button._4).getPollData()!=0f;
			buttons[5] = controller.getComponent(Identifier.Button._5).getPollData()!=0f;
			
			buttons[6] = controller.getComponent(Identifier.Button._8).getPollData()!=0f;
			buttons[7] = controller.getComponent(Identifier.Button._9).getPollData()!=0f;
			
			buttons[8] = controller.getComponent(Identifier.Button._7).getPollData()!=0f;
			buttons[9] = controller.getComponent(Identifier.Button._6).getPollData()!=0f;
			//A,B,X,Y,LB,RB,LStick,RStick,Start,Back,//HAT
			
			Log.info("Axis "+controller.getComponent(Identifier.Axis.X));
			
			float triggerValue = controller.getComponent(Identifier.Axis.Z).getPollData();
            if (triggerValue > 0)
                    {lt = triggerValue;
            		rt=0f;}
            else
                    {rt = -triggerValue;
                    lt=0f;}
            
		}
	}

	@Override
	public void init() {
		
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] controllers = ce.getControllers();

		id=-1;
		for(int i=(controllers.length-1);i>-1;i--){
			Log.info("Found "+controller.getName());
			if(controllers[i].getName().toLowerCase().contains("xbox")){
				id=i;
				Log.debug("Found an Xbox controller, id is "+id);
				Log.debug("Has "+controllers[i].getComponents()[0].getName());
				connected=true;
				
				//TODO either EventQueue or get buttons NOW?
				
				//Identifier.Button.
			}
		}
		
		if(id==-1){
			Log.info("No JInput controllers found.");
			connected=false;
		}
		
		controller = controllers[id];
		
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
		return buttons;
	}

	@Override
	public float getForward() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getRight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getLookRight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getLookUp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rumbleOff() {
		Log.info("Rumble not supported by XInput");
	}

	@Override
	public float getLeftTrigger() {
		return lt;
	}

	@Override
	public float getRightTrigger() {
		return rt;
	}

}
