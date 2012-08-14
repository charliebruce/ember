package net.ember.graphics;

public class PointLight extends Light {

	
	@Override
	public String getName() {
		return "Point Light at "+position.toString();
	}

	@Override
	public void init() {
		isPhysical = false;
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
