package net.ember.graphics;

import net.ember.game.Entity;

/**
 * Any visible light in the world.
 * @author Peter
 *
 */
public abstract class Light extends Entity {

	/**
	 * Does the light cast shadows?
	 */
	public boolean castsShadows;
	
	/**
	 * Does the light's shadow map change?
	 * Dynamic shadows always display up-to-date information, and use less memory (it reuses the "reserved" one), but require a (fairly cheap) graphical pass per light.
	 */
	public boolean dynamicShadows;
	
	/**
	 * Does the light cast a custom map?
	 * Spotlights or projectors might use this to project a specific pattern onto surfaces. Focus is NOT emulated, it's "coherent" (Abuse of term?) 
	 */
	public boolean castsImage;
	
}
