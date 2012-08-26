package net.ember.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

import net.ember.client.Client;
import net.ember.data.ModelLoader;
import net.ember.data.OrderedStaticModel;
import net.ember.data.OrderedStaticModelLoader;
import net.ember.data.MaterialFileLoader;
import net.ember.data.Model;
import net.ember.data.PhysicsMesh;
import net.ember.data.PhysicsMeshLoader;
import net.ember.filesystem.Filesystem;
import net.ember.game.entities.Ball;
import net.ember.graphics.Material;
import net.ember.logging.Log;
import net.ember.physics.Physics;

/**
 * A region is a small piece ("chunk") of the world containing:
 * Geometry (for graphics and physics)
 * Entities:
 * AI entities
 * Sound and particle emitter objects
 * 
 * Scripts
 * 
 * It also handles:
 * Metadata (location, resources required)
 * 
 * Only one region should ever be visible, and normally only two will be in memory.
 * Essentially the world is a double buffered set of two regions.
 * 
 * Assuming the tools can handle several hundred regions, each region will be made 
 * using a tool which explodes an entire set of region into boxes (artist or program-decided)
 * 
 * The physics world has a maximum size of a few km for performance, so if we want to expand
 * the world beyond these limits whilst keeping the continuity, we will need to shift the
 * origin of the world. Rather than this, each continuous area should be less than 10km,
 * and simply teleport between two or more self-contained areas.
 * 
 * WOLFIRE:
 * Level
 * Terrain Object
 * Terain Tex
 * Terrain Cache
 * Terrain->Physics
 * Spawns
 * Groups/Contents
 * "Movement Objects"
 * env objects
 * decals
 * hotspots
 * Loaded Objects
 * Shadows
 * Nav
 * Ambient Sound/Mus
 * Sky
 * 
 * @author Charlie
 *
 */
public class Region {
	
	private int id;
	private boolean active = false;
	private boolean loaded = false;
	
	
	
	public OrderedStaticModel regionGraphicsMesh;
	private PhysicsMesh regionPhysicsMesh;
	private RigidBody regionPhysicsMeshBody;
	

	/**
	 * Linked list used since we only ever append or read sequentially.
	 */
	private List<Entity> entities = new LinkedList<Entity>();
	
	
	/**
	 * Create a Region object with the given ID number.
	 * The region's metadata is read at this point.
	 * @param id
	 */
	public Region(int id){
		
	}
	
	
	/**
	 * Load all of the files into memory. This is called in its own thread by a resource manager?!!?!?!?!?!??!!?!?
	 */
	public void load(){
		
		/**
		 * Work out which resources are required by this region but not loaded.
		 */
		
		
		
		
	
	
		
		/**
		 * Queue them for load/load them
		 */
		//Client.loadManager. (IS IT AN ISSUE TO CALL A METHOD TO AN OBJECT CALLING ME?!)
		
		
		try {
			regionGraphicsMesh = (OrderedStaticModel) ModelLoader.get(Filesystem.get("regions/"+id+"/graphics.mesh"), ""+id);
		} catch (FileNotFoundException e1) {
			Log.warn("Graphics mesh for region "+id+" not found! Enjoy stepping on an invisible land.");
		}
		
		
		try {
			regionPhysicsMesh = PhysicsMeshLoader.get(Filesystem.get("regions/"+id+"/physics.mesh"));
		} catch (FileNotFoundException e) {
			Log.warn("Physics mesh for region "+id+" not found! Enjoy falling through the world.");
		}
		
		
		//Nav mesh
		/**
		 * Wait for the textures to load.
		 */
		
		
		
		
	
		
		loaded=true;
	}
	
	/**
	 * Make this region "active" (playable)
	 * This process should be inexpensive - a region is activated pre-emptively.
	 */
	public void activate(){
		
		if(!loaded){
			Log.warn("Region becoming active before load is complete!");
			//Stall until the load manager is up to date.
			int i=0;
			while(!Client.loadManager.idle()){try {
				Thread.sleep(100);
				i++;
				if(i==100)
					Log.err("Stall for over 10 seconds activating region "+id+" and waiting to become loaded. The region might not have bean load() ed!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}}
		}

		/**
		 * Initialise any entities we might rely on. 
		 */
		for(int i=0;i<10;i++)
		entities.add(new Ball());
		
		
		//Move in the entities
		//If an AI spawner is created, or a script creates an AI, then they should also all be destroyed at some point.
		//Entities which can roam from multiple regions should also be considered carefully!
		for(Entity e: entities){
			World.addEntity(e);
		}
		
		
		//The graphical geometry and entity models/props etc also need to be put onto the graphics card
		
		//The physics engine should receive the new geometry. For now we assume coordinates in the file are "absolute"  
		regionPhysicsMeshBody = Physics.addStaticMesh(regionPhysicsMesh, new Vector3f(0.0f,0.0f,0.0f), new Quat4f(0.0f,0.0f,0.0f,1.0f));
		
		//The sound system should also receive the first samples of any potential audio tracks as a cache... TODO this.
		
		
		//Finally any low-resolution proxies of this region should be vanquished ie towers in the distance.
		
		
		
		//We're done!
		active = true;
	}
	
	/**
	 * The player must no longer be in this region - de-activate it for speed and to prepare to load another region instead.
	 */
	public void deactivate(){
		if(!active) return;
		
		//Remove the physics objects/terrain.
		Physics.removeBody(regionPhysicsMeshBody);
		
		//Remove the extraneous sound caches, textures where applicable.
		
		//Remove all entities from the world, and their side-effect resources where applicable.
		for(Entity e: entities){
			World.removeEntity(e);
			e.destroy();
			e=null;
		}
		entities.clear();
		
		
		
	}
	
	/**
	 * Remove all of the resources this region mandated.
	 */
	public void unload(){
		
		/**
		 * Work out which of the resources required by this region are NOT also required by others, and remove them
		 * This might be as simple as looking at what this region caused to be loaded in the first place that does not intersect the future region.
		 */
		
		/**
		 * Remove them.
		 */

		/**
		 * Misc
		 */
		regionPhysicsMeshBody.destroy();
		regionPhysicsMeshBody = null;
		
		loaded=false;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public boolean isLoaded(){
		return loaded;
	}
	

}
