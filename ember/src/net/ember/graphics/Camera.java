package net.ember.graphics;

import javax.vecmath.Vector3f;

import net.ember.client.Preferences;
import net.ember.game.Entity;
import net.ember.game.World;
import net.ember.game.entities.Player;
import net.ember.math.Matrix;
import net.ember.math.Vector;

public class Camera extends Entity {
	
	public Camera(){
		position.y=0.5f;
		isVisible=false;
	}
	
	/**
	 * Pixel dimensions
	 */
	public int width=Preferences.targetWidth;
	public int height=Preferences.targetHeight;

	
	/**
	 * Near and far distances - 1km to 1cm
	 */
	public float zFar = (float) 1000.0;
	public float zNear = (float) 0.01;
	

	/**
	 * Direction
	 */
	protected float elevation=(float) (0.5f*Math.PI);
	protected float azimuth=0.0f;
	
	public boolean rebuildTransform=true;
	public boolean rebuildProjection=true;
	public boolean rebuildFrustrumPlanes=true;

	
	private float[][] cachedFrustrumPlanes = new float[6][4];
	/**
	 * Return the camera's six frustrum planes
	 * Normals face OUTWARDS in a RHS
	 * TODO check me and my functions called
	 * they might be complete BS
	 * @return
	 */
	public float[][] getFrustrumPlanes() {
		//if(rebuildFrustrumPlanes)
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!rebuildFrustrumPlanes();

		return cachedFrustrumPlanes;
	}
	/*
	private void rebuildFrustrumPlanes() {
		rebuildFrustrumPlanes=false;
		//Log.info("Rebuilding frustrum planes.");
		//Directions
		float[] viewdir = Vector.normalise3(getViewDirection());
		float[] rightdir = Vector.normalise3(Vector.crossProduct3(new float[]{0.0f,1.0f,0.0f}, viewdir));		
		float[] liftdir = Vector.normalise3(Vector.crossProduct3(rightdir, viewdir));
		//Log.info("Unit vector directions are "+Vector.toString3(viewdir)+","+Vector.toString3(rightdir)+","+Vector.toString3(liftdir));
		
		//fovs
		float aspect = (float)width/(float)height;
		float fovyrad = (float) Math.toRadians((Preferences.fovx/2)/aspect);//TODO check atan 
		float fovxrad = (float) Math.toRadians(Preferences.fovx/2);
		
		//Log.info("fovxlen on far is "+(zFar*Math.tan(fovxrad))+" and other is "+(zFar*Math.tan(fovyrad)));
		
		//Vectors used in construction
		float halfnearwidth = (float) (zNear*Math.tan(fovxrad));
		float halfnearheight = (float) (zNear*Math.tan((fovyrad)));//Or atan here
		float halffarwidth = (float) (zFar*Math.tan(fovxrad));
		float halffarheight = (float) (zFar*Math.tan((fovyrad)));
		
		float[] vecNear = Vector.multiplyScalar3(zNear, viewdir);
		float[] vecFar = Vector.multiplyScalar3(zFar, viewdir);
		
		float[] vecFarToRight = Vector.multiplyScalar3(halffarwidth, rightdir);
		float[] vecFarToLeft = Vector.multiplyScalar3(-halffarwidth, rightdir);
		float[] vecFarToTop = Vector.multiplyScalar3(halffarheight, liftdir);
		float[] vecFarToBottom = Vector.multiplyScalar3(-halffarheight, liftdir);
		
		float[] vecNearToRight = Vector.multiplyScalar3(halfnearwidth, rightdir);
		float[] vecNearToLeft = Vector.multiplyScalar3(-halfnearwidth, rightdir);
		float[] vecNearToTop = Vector.multiplyScalar3(halfnearheight, liftdir);
		float[] vecNearToBottom = Vector.multiplyScalar3(-halfnearheight, liftdir);
		
		
		//Points are position + (near or far) + (left or right) + (up or down)

		//(TOP/BOTTOM)(LEFT/RIGHT)(NEAR/FAR)
		float[] trf = Vector.add3(Vector.add3(position, vecFar),Vector.add3(vecFarToRight, vecFarToTop));
		float[] brf = Vector.add3(Vector.add3(position, vecFar),Vector.add3(vecFarToRight, vecFarToBottom));
		float[] tlf = Vector.add3(Vector.add3(position, vecFar),Vector.add3(vecFarToLeft, vecFarToTop));
		float[] blf = Vector.add3(Vector.add3(position, vecFar),Vector.add3(vecFarToLeft, vecFarToBottom));
		
		float[] trn = Vector.add3(Vector.add3(position, vecNear),Vector.add3(vecNearToRight, vecNearToTop));
		float[] brn = Vector.add3(Vector.add3(position, vecNear),Vector.add3(vecNearToRight, vecNearToBottom));
		float[] tln = Vector.add3(Vector.add3(position, vecNear),Vector.add3(vecNearToLeft, vecNearToTop));
		float[] bln = Vector.add3(Vector.add3(position, vecNear),Vector.add3(vecNearToLeft, vecNearToBottom));
						
		//Log.info("Points sample are "+Vector.toString3(blf)+","+Vector.toString3(tlf)+","+Vector.toString3(tln));
		
		
		
		//NOTE ORDER DEFINES SIGN OF NORMALS!!!!! THEY MUST FACE OUTWARDS!
		//Clockwise when normal is pointing at you.
		//TODO THE FIRST IS MEANT TO BE blf,tlf,tln
		cachedFrustrumPlanes[0]=Plane.fromPoints(blf,tlf,tln);
		cachedFrustrumPlanes[1]=Plane.fromPoints(tln,tlf,trf);
		cachedFrustrumPlanes[2]=Plane.fromPoints(trn,trf,brf);
		
		cachedFrustrumPlanes[3]=Plane.fromPoints(brn,brf,blf);
		cachedFrustrumPlanes[4]=Plane.fromPoints(blf,trn,brn);
		cachedFrustrumPlanes[5]=Plane.fromPoints(brf,trf,tlf);
		
		
		//Log.debug("Rebuild frustrum planes done. Left plane equation is "+Plane.toString(cachedFrustrumPlanes[0]));
	}
*/


	public void setPosition(float[] pos){
		
			position.x=pos[0];
			position.y=pos[1];
			position.z=pos[2];
			rebuildTransform=true;
			rebuildFrustrumPlanes=true;
		
	}


	public float getAzimuth() {
		return azimuth;
	}

	public float getElevation() {
		return elevation;
	}


	public float[] getViewDirection() {
		return new float[]{(float) (Math.cos(azimuth)*Math.sin(elevation)),(float) Math.cos(elevation),(float) (Math.sin(azimuth)*Math.sin(elevation))};
	}



/**
 * Return the "Right" direction to the viewer. 
 * @return
 */
	public float[] getRight() {
		return Vector.crossProduct3(getViewDirection(), new float[]{0.0f,1.0f,0.0f});
	}
	
	public void setOrientation(float azi, float ele) {
		//TODO check if no change exists
		rebuildTransform=true;
		rebuildFrustrumPlanes=true;
		azimuth = azi;
		elevation = ele;
		if(ele<(Math.PI/2.0)) ele=(float) (Math.PI/2.0);
		if(ele>Math.PI) ele=3.141f;
		float num = (float) (azimuth%(2*Math.PI));//keep precision by preventing massive rotation
		azimuth = num;

	}
	
	
	
	
	
	
	
	
	

	
	private float[] cachedProjection;
	private float[] viewMatrix;
	

	
	


	
	//Equation is fovx = atan(tan(fovy/2)*width/height)*2) RADIANS
	public float[] projection(){
		if(rebuildProjection)
			recalculateProjection();
		return cachedProjection;
	}

	public void recalculateProjection() {
		rebuildProjection = false;
		/*
		Given f defined as follows:
		aspect=w/h
		f = cotangent fovy 2

		*/
		if(height<1)height = 1;//Prevent / by 0

		float aspect = (float)width/(float)height;
		float fovy = Preferences.fovx/aspect;
		
		float f = (float) (1.0/Math.tan(Math.toRadians(fovy/2)));

		//By http://www.manpagez.com/man/3/gluPerspective/ 
		cachedProjection = new float[]{f/aspect, 0,0,0,0,f,0,0,0,0,(zFar+zNear)/(zNear-zFar),-1,0,0, (2*zFar*zNear)/(zNear-zFar), 1.0f/*0???*/};

	}

	int lookmode = 0;//0 point 1 free 2 fwd 3 back 4 left 5 right
	public float[] transformation() {
		
		float[] targ = new float[3];
		//TODO prevent overflow
		float[] dir = getViewDirection();
		targ[0]=(float) (position.x+dir[0]);
		targ[1]=(float) (position.y+dir[1]);
		targ[2]=(float) (position.z+dir[2]);
		
		lookAt(position,targ[0],targ[1],targ[2]);//0 2 -1
	

		return viewMatrix;
	}
	

	
	//BELOW FROM LIGHTHOUSE3D
	 
	// ----------------------------------------------------
	// View Matrix
	//
	// note: it assumes the camera is not tilted,
	// i.e. a vertical up vector
	//
	 
	void lookAt(Vector3f position,
	               float lookAtX, float lookAtY, float lookAtZ) {
	 rebuildTransform=false;
	    float[] dir = new float[3];
	    float[] right = new float[3];
	    float[] up = new float[3];
	 
	    up[0] = 0.0f;   up[1] = 1.0f;/*1*/   up[2] = 0.0f;/*0*/
	 
	    dir[0] =  (lookAtX - position.x);
	    dir[1] =  (lookAtY - position.y);
	    dir[2] =  (lookAtZ - position.z);
	    dir = Vector.normalize3(dir);
	 
	    right = Vector.crossProduct3(dir,up);
	    right = Vector.normalize3(right);
	 
	    up = Vector.crossProduct3(right,dir);
	    up = Vector.normalize3(up);
	 
	    viewMatrix=new float[16];
	    
	    viewMatrix[0]  = right[0];
	    viewMatrix[4]  = right[1];
	    viewMatrix[8]  = right[2];
	    viewMatrix[12] = 0.0f;
	 
	    viewMatrix[1]  = up[0];
	    viewMatrix[5]  = up[1];
	    viewMatrix[9]  = up[2];
	    viewMatrix[13] = 0.0f;
	 
	    viewMatrix[2]  = -dir[0];
	    viewMatrix[6]  = -dir[1];
	    viewMatrix[10] = -dir[2];
	    viewMatrix[14] =  0.0f;
	 
	    viewMatrix[3]  = 0.0f;
	    viewMatrix[7]  = 0.0f;
	    viewMatrix[11] = 0.0f;
	    viewMatrix[15] = 1.0f;
	    
	    float[] aux = new float[16];
	 
	    aux = Matrix.translationMatrix(-position.x, -position.y, -position.z);
	 
	    viewMatrix = Matrix.multMatrix(viewMatrix, aux);
	}
	


	public float[] inverseProjection() {
		recalculateProjection();
		float[] thingy = Matrix.inverse4(cachedProjection);
		//System.out.println("CP:"+Matrix.string4(thingy));
		//Log.debug(Matrix.string4(thingy)+" is inverse of \n"+Matrix.string4(cachedProjection));
		return thingy;
	}

	@Override
	public String getName() {
		return "Camera";
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick() {
		Player p = World.player;
		//Hover behind the player.
		float[] viewdir = getViewDirection();
		Vector3f vd = new Vector3f(viewdir);
		vd.normalize();
		vd.scale(-1.75f);
		vd.add(p.position);
		position.set(vd);
	}

	public float[] normalmatrix() {
		return Matrix.normalmatrix(transformation());
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}


	
	
}
