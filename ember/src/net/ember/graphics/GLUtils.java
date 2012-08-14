package net.ember.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.ember.filesystem.Filesystem;
import net.ember.logging.Log;

public class GLUtils {


	public static int loadFullscreenVBO(GL2 gl){

		//gl.glUseProgram(test.getShaderProgram());
		int[] target = new int[1];
		gl.glGenBuffers(1, target, 0);

		//float[] data = new float[]{-1,1,1,1,-1,-1,1,-1};
		float[] data = new float[]{1,-1,1,1,-1,-1,-1,-1,1,1,-1,1};
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, target[0]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, 6*2*4/*4points*2variables*4bpfloat*/,FloatBuffer.wrap(data), GL.GL_STATIC_DRAW);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

		gl.glEnable(GL.GL_ARRAY_BUFFER);

		return target[0];

		//gl.glUseProgram(0);
	}
	
	
	public static void capture(GL2 gl){
		//byte requires 4
		int num = 3;//Bytes per pixel
		ByteBuffer target = ByteBuffer.allocate(num/*Correct?*/*Graphics.camera.width*Graphics.camera.height);
		gl.glReadPixels(0, 0, Graphics.camera.width,Graphics.camera.height,GL.GL_RGB, GL2.GL_BYTE, target);//ORIG = rgba, unsigned byte
		
		
		BufferedImage img = new BufferedImage(Graphics.camera.width, Graphics.camera.height, BufferedImage.TYPE_INT_RGB);//TODO type

		java.awt.Graphics g = img.getGraphics();
		for(int h = 0; h<Graphics.camera.height;h++){
		for (int w=0;w<Graphics.camera.width;w++){
			
				int startindex = num*(h*Graphics.camera.width+w);
				//System.out.println(Graphics.camera.height-h);
				//img.setRGB(w,Graphics.camera.height-h-1,/*16777215-*/(arr[startdex+2]));
				//img.setRGB(w,h,1,1,arr[startdex],arr[startdex+1],arr[startdex+2]);
				byte r=target.get();byte gr=target.get();byte b=target.get();
				Color c = new Color(r,gr,b);
				g.setColor(c);
				
				g.drawRect(w,Graphics.camera.height-h,  1, 1);
			}
		}



		try {
			ImageIO.write(img, "png", new FileOutputStream(Filesystem.getWrite("out/cap.png").getFD()));
			Log.info("Captured frame to data/out/cap.png");
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.warn("Could not capture image to data/out/cap.png");
		}
	}


	/**
	 * Return the transformation matrix for a given position, orientation and scale
	 * @param position
	 * @param orientation Quat, WXYZ
	 * @param scale
	 * @return
	 */
	public static final float[] getTransformationMatrix(float[] position,
			float[] orientation, float scale) {
		float sqw = orientation[0]*orientation[0];
		float sqx = orientation[1]*orientation[1];
		float sqy = orientation[2]*orientation[2];
		float sqz = orientation[3]*orientation[3];

		float[] mout = new float[16];
		// invs (inverse square length) is only required if quaternion is not already normalised
		// also scale
		float invs = scale*( 1 / (sqx + sqy + sqz + sqw));
		mout[0] = ( sqx - sqy - sqz + sqw)*invs ; // since sqw + sqx + sqy + sqz =1/invs*invs
		mout[5] = (-sqx + sqy - sqz + sqw)*invs ;
		mout[10] = (-sqx - sqy + sqz + sqw)*invs ;

		float tmp1 = orientation[1]*orientation[2];
		float tmp2 = orientation[3]*orientation[0];
		
		mout[1] = 2.0f * (tmp1 + tmp2)*invs ;
		mout[4] = 2.0f * (tmp1 - tmp2)*invs ;

		tmp1 = orientation[1]*orientation[3];
		tmp2 = orientation[2]*orientation[0];
		mout[2] = 2.0f * (tmp1 - tmp2)*invs ;
		mout[8] = 2.0f * (tmp1 + tmp2)*invs ;
		
		tmp1 = orientation[2]*orientation[3];
		tmp2 = orientation[1]*orientation[0];
		mout[6] = 2.0f * (tmp1 + tmp2)*invs ;
		mout[9] = 2.0f * (tmp1 - tmp2)*invs ;      
		
		mout[12]=position[0];
		mout[13]=position[1];
		mout[14]=position[2];
		mout[15]=1.0f;
		return mout;
		
		
		
		/*
		 * The equivalent rotation matrix representing a quaternion is
		 

		Matrix =  [ w2 + x2 - y2 - z2       2xy - 2wz           2xz + 2wy
		                2xy + 2wz       w2 - x2 + y2 - z2       2yz - 2wx
		                2xz - 2wy           2yz + 2wx       w2 - x2 - y2 + z2 ]


		Using the property of unit quaternions that w2 + x2 + y2 + z2 = 1, we can reduce the matrix to

		Matrix =  [ 1 - 2y2 - 2z2    2xy - 2wz      2xz + 2wy
		              2xy + 2wz    1 - 2x2 - 2z2    2yz - 2wx
		              2xz - 2wy      2yz + 2wx    1 - 2x2 - 2y2 ]
		 */
	}





}
