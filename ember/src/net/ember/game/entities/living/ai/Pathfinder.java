package net.ember.game.entities.living.ai;

import net.ember.game.Entity;
import net.ember.game.Living;

public class Pathfinder {

	//naive method: precompute per-node cost to reach all other nodes
	//1k nodes approx smb, 10k nodes 190mb
	
	//Djikstra or A* or my own "broad-fine" algo? Test it out and remember precompute idea with weighting.

	//Or very simple, assume an open arena and close-distance combat/spawning only.
	//Then just avoid hazards, push as close to the player horizontally as possible.
	
	public static void moveTowards(Living e, Entity towards){
	
		e.movement.sub(towards.position, e.position);
		e.movement.y=0;
		e.movement.normalize();
		e.movement.scale(2.5f);//speed?
		
	}
	
	
}
