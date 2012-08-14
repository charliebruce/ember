package net.ember.input;

/**
 * A physical gamepad (controller).
 * For now this is just an interface for Xbox controllers.
 * @author Charlie
 *
 */
public interface Gamepad {


	/**
	 * Obtain the data from the controller/API.
	 */
	abstract void poll();

	/**
	 * Choose a sensible physical object, get its capabilities etc.
	 */
	public abstract void init();

	/**
	 * Check for addition, removal, battery change etc.
	 */
	abstract void checkStatus();
	
	abstract boolean isConnected();

	/**
	 * Get an array of booleans with the state of each button.
	 * Order for Xbox:
	 * A,B,X,Y,LB,RB,LStick,RStick,Start,Back,//HAT
	 * @return
	 */
	abstract boolean[] getButtons();
}
