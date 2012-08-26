package net.ember.game;

import java.util.ArrayList;
import java.util.List;

import net.ember.client.Client;
import net.ember.game.entities.Player;
import net.ember.graphics.Graphics;
import net.ember.physics.Physics;

/**
 * Everything gameplay-related is pretty much entirely handled by the world. 
 * @author Charlie
 *
 */
public class World {

	/**
	 * Here are our two buffered regions.
	 */
	public static Region[] regions = new Region[2];
	
	
	public static List<Entity> entities = new ArrayList<Entity>();
	
	public static Player player;
	
	/**
	 * Initialise the world. For now we just load the first region of the game. This will be selected in a main menu eventually.
	 */
	public static void init() {
		
		Client.loadManager.loadRegion(0,0);
		
		
	}


	public static void addEntity(Entity e) {
		if (e.isPhysical)
			Physics.addEntity(e);
		entities.add(e);
	}


	/**
	 * Advance the world by one tick.
	 */
	public static void tick() {
		// TODO Auto-generated method stub
		
		/*
		 * Simple volume tests for region triggers etc. 
		 */
		
		
		/*
		 * Tick entities and their AI?¬?¬?¬?
		 * 
		 */
		for(Entity e:entities){
			e.tick();
		}
	}


	public static void removeEntity(Entity e) {
		//TODO also unset and remove it.
		entities.remove(e);
	}

	/*
	 * TODO TODO check performance. Also allocate space.
	 */
	private static List<Entity> visset = new ArrayList<Entity>();

	public static List<Entity> getVisibleEntities() {
		// TODO Auto-generated method stub
		visset.clear();
		for(Entity e: entities){
			if(e.isVisible)
				visset.add(e);
		}
		return visset;
	}
	
	public static void createPlayer(){
		player = new Player();
		player.init();
		addEntity(player);
		addEntity(Graphics.camera);
	}
	

}
