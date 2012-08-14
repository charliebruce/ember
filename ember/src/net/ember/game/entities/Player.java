package net.ember.game.entities;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CapsuleShape;

import net.ember.game.Entity;
import net.ember.game.Living;
import net.ember.graphics.Graphics;
import net.ember.logging.Log;
import net.ember.physics.CharacterController;

public class Player extends Living {

	
	public Player(){
		collisionShape = new CapsuleShape(0.3f,1.8f);
		model = Graphics.renderer.getModel("unitsphere");
	}

	@Override
	public String getName() {
		return "Player";
	}

	@Override
	public void init() {
		cc = new CharacterController();
		cc.init(this);
	}

	@Override
	public void tick() {
		//To enable double jumping
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void jump() {
		cc.character.jump();
		//cc.character.verticalVelocity+=10f;
	}
}
