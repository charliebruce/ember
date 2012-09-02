package net.ember.game.entities.living;

import javax.vecmath.Vector3f;

import net.ember.game.Entity;
import net.ember.game.Living;
import net.ember.game.World;
import net.ember.game.entities.living.ai.Pathfinder;

/**
 * Code which provides a simple, adaptable base for generic intelligent creatures.
 * 
 * @author Administrator
 *
 */
public abstract class BaseIntelligence extends Living {

	//TODO replace this with some more robust code, which prioritises targets, 
	//and allows for variable teams etc.
	boolean alert = false;
	boolean hostileToPlayer = true;
	Entity target=null;
	int team = 0;
	
	//Default to a lunge attack.
	boolean melee = true;
	

	
	
	
	@Override
	public void tick(){
		//First, check if we have been injusred, or alerted by the presence of an opponent.
		if(!alert){
			if(hostileToPlayer&&closeEnoughToAlert(World.player))
				{alert=true;target=World.player;}
		}
		
		if(alert&&null!=target){
			//For now, advance towards the player
			Pathfinder.moveTowards(this, target);
			
			
			//If within attacking range but not yet attacking, and not stunned, do so.
			
			
			
		}
		
	}
	
	
	/**
	 * Is the given entity close enough to trigger me?
	 * For now, this just triggers at <15m
	 * We could add area triggers instead?
	 * @param e
	 * @return
	 */
	public boolean closeEnoughToAlert(Entity e){
		Vector3f distance = new Vector3f();
		distance.sub(e.position, position);
		
		if(distance.length()<15.0f) return true;
		return false;
	}
}
