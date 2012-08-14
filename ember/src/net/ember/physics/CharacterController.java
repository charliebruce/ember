package net.ember.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.ember.game.Entity;
import net.ember.game.Living;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
//import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.Transform;

/**
 * Handles interaction between characters and physics system.
 * @author Charlie
 *
 */
public class CharacterController {

	public KinematicCharacterController character;
	public PairCachingGhostObject ghostObject;

	/*
	 * See JBullet Character Demo
	 * Use a Ghost Object and capsule shape for motion.
	 * 
	 * Or just implement a capsule being moved and deal with resolving collisions?!
	 * Slope angle for sliding etc.
	 */
		
	float characterScale=1.0f;
	Living e;
	public void init(Living controlling){
		
		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(0.0f, 4.0f, 0.0f);

		ghostObject = new PairCachingGhostObject();
		ghostObject.setWorldTransform(startTransform);
		Physics.overlappingPairCache.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		float characterHeight = 0.75f * characterScale;
		float characterWidth = 0.75f * characterScale;
		ConvexShape capsule = new CapsuleShape(characterWidth, characterHeight);
		ghostObject.setCollisionShape(capsule);
		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		float stepHeight = 0.35f * characterScale;
		character = new KinematicCharacterController(ghostObject, capsule, stepHeight);
		//Or implement own based on KCC
		
		//TODO check collisions with world, platforms only, hook into gameplay logic for platforms, region triggers etc, implement jump and slide control.
		Physics.dynamicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));

		Physics.dynamicsWorld.addAction(character);
		
		Physics.addCC(this);
		e=controlling;
	}
	
	//S.B. called before stepSimulation.
	public void tick(){
		//Transform xform = ghostObject.getWorldTransform(new Transform());

		/*Vector3f forwardDir = new Vector3f();
		xform.basis.getRow(2, forwardDir);
		//printf("forwardDir=%f,%f,%f\n",forwardDir[0],forwardDir[1],forwardDir[2]);
		Vector3f upDir = new Vector3f();
		xform.basis.getRow(1, upDir);
		Vector3f strafeDir = new Vector3f();
		xform.basis.getRow(0, strafeDir);
		forwardDir.normalize();
		upDir.normalize();
		strafeDir.normalize();*/

		//float walkVelocity = 1.1f * 4.0f; // 4 km/h -> 1.1 m/s
		//float walkSpeed = walkVelocity * .005f * characterScale;

		/*
		if (gLeft != 0) {
			walkDirection.add(strafeDir);
		}

		if (gRight != 0) {
			walkDirection.sub(strafeDir);
		}*/

		
/*
		if (gBackward != 0) {
			walkDirection.sub(forwardDir);
		}
*/
		character.setWalkDirection(e.movement);

	}

	private Transform t = new Transform();
	private Quat4f ori = new Quat4f();
	public void move() {
		t.getRotation(ori);
		ghostObject.getWorldTransform(t);
		e.position.set(t.origin);
		e.orientation.set(ori);
	}
	
	
	
	
}
