package net.ember.game.entities;

import com.bulletphysics.collision.shapes.SphereShape;

import net.ember.game.Entity;
import net.ember.graphics.Graphics;

public class Ball extends Entity {

	public Ball(){
		collisionShape = new SphereShape(0.5f);
		isPhysical=true;
		position.y=(float) (10.0f+Math.random()*10f);
		position.z=(float) (Math.random()*5.0f);
		position.x=(float) (Math.random()*5.0f);
		mass=10f;
		model = Graphics.renderer.getModel("unitsphere");
	}
	@Override
	public String getName() {
		return "Ball";
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
