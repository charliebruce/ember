package net.ember.game;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.ember.data.Model;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;

/**
 * Any logical object in the game world.
 * @author Charlie
 *
 */
public abstract class Entity {

	/**
	 * The position of the object at the given frame.
	 */
	public Vector3f position = new Vector3f(0.0f,0.0f,0.0f);
	
	/**
	 * The velocity of the object - used for motion blur and doppler shift only, really.
	 */
	public Vector3f velocity = new Vector3f(0.0f,0.0f,0.0f);
	
	/**
	 * The object's orientation in 3d space.
	 */
	public Quat4f orientation = new Quat4f(0.0f,0.0f,0.0f,1.0f);
	
	/**
	 * The object's mass. 0 mass stops it from being moved.
	 */
	public float mass;
	
	/**
	 * Does the object interact with physical objects (collisions)?
	 */
	public boolean isPhysical;
	
	/**
	 * Any physical entities should implement a rigid body.??
	 */
	public RigidBody rigidBody;
	public CollisionShape collisionShape;
	/**
	 * The entity should have a name to identify it.
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * The entity should also have a global scale to bring its model into correct units.
	 * This could also make for some fun game modes! Shrink-gun anyone? :)
	 * This should feed into the physics system too?
	 */
	public float scale = 1.0f;

	/**
	 * To prevent an entity from being rendered this should be false.
	 * Logical but invisible entities should override this.
	 * This is just a way of quickly excluding an entity from ever possibly being rendered.
	 * It does NOT indicate occlusion!
	 */
	public boolean isVisible = true;

	/**
	 * The graphical (visible, real-world) visible portion of this entity.
	 * This is probably a pointer to the StaticModel or AnimatedModel used.
	 * AnimatedModels should also have an associated AnimationState
	 * (So multiple entities with the same meshes etc can use same data)
	 */
	public Model model = null;
	
	
	/**
	 * Called on creation.
	 */
	public abstract void init();
	
	/**
	 * Tick is called every frame.
	 */
	public abstract void tick();

	/**
	 * When the entity is removed from existence.
	 */
	public abstract void destroy();
	
}
