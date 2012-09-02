package net.ember.graphics;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.ember.logging.Log;

public class Utils {
	public static final int loadFullscreenVBO(GL2 gl){
		
		//gl.glUseProgram(test.getShaderProgram());
		int[] target = new int[1];
		gl.glGenBuffers(1, target, 0);
		
		
		//float[] data = new float[]{-1,1,1,1,-1,-1,1,-1};
		float[] data = new float[]{1,-1,1,1,-1,-1,-1,-1,1,1,-1,1};
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, target[0]);
		
		
		gl.glBufferData(GL.GL_ARRAY_BUFFER, 6*8/*4points*2variables*4bpfloat*/,FloatBuffer.wrap(data), GL.GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

		//gl.glEnable(GL.GL_ARRAY_BUFFER);
		Log.debug("Loading the full-screen VBO.");
		Render.assertNoError(gl);
		return target[0];
	}

	public static float[] transformationMatrix(Vector3f position,
			Quat4f orientation, float scale) {
		
		float sqw = orientation.w*orientation.w;
		float sqx = orientation.x*orientation.x;//1
		float sqy = orientation.y*orientation.y;
		float sqz = orientation.z*orientation.z;//3

		float[] mout = new float[16];
		// invs (inverse square length) is only required if quaternion is not already normalised
		// also scale
		float invs = scale*( 1 / (sqx + sqy + sqz + sqw));
		mout[0] = ( sqx - sqy - sqz + sqw)*invs ; // since sqw + sqx + sqy + sqz =1/invs*invs
		mout[5] = (-sqx + sqy - sqz + sqw)*invs ;
		mout[10] = (-sqx - sqy + sqz + sqw)*invs ;

		float tmp1 = orientation.x*orientation.y;
		float tmp2 = orientation.z*orientation.w;
		mout[1] = 2.0f * (tmp1 + tmp2)*invs ;
		mout[4] = 2.0f * (tmp1 - tmp2)*invs ;

		tmp1 = orientation.x*orientation.z;
		tmp2 = orientation.y*orientation.w;
		mout[2] = 2.0f * (tmp1 - tmp2)*invs ;
		mout[8] = 2.0f * (tmp1 + tmp2)*invs ;
		
		tmp1 = orientation.y*orientation.z;
		tmp2 = orientation.x*orientation.w;
		mout[6] = 2.0f * (tmp1 + tmp2)*invs ;
		mout[9] = 2.0f * (tmp1 - tmp2)*invs ;      
		
		mout[12]=position.x;
		mout[13]=position.y;
		mout[14]=position.z;
		mout[15]=1.0f;
		return mout;
		
	}

}
