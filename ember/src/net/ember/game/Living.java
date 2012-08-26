package net.ember.game;

import javax.vecmath.Vector3f;

import net.ember.physics.CharacterController;

public abstract class Living extends Entity {

	public Vector3f movement = new Vector3f(0.0f,0.0f,0.0f);
	protected CharacterController cc;
	
	public Living(){
		cc=new CharacterController();
		cc.init(this);
	}
}
