package net.ember.graphics.shaders;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

import net.ember.filesystem.Filesystem;
import net.ember.graphics.Renderer;
import net.ember.logging.Log;

/**
 * A wrapper to simplify the process of creating a shader.
 * @author Charlie
 *
 */
public abstract class Shader {

	public int id;
	
	private int vertid,fragid;
	
	
	
	/**
	 * Load the shader into GL and its locations into memory.
	 * @param gl
	 */
	public void load(GL2 gl){
		
		gl.glUseProgram(0);
		
		vertid = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		fragid = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);

		String temp;
		
		temp = loadFile(getFragmentCodeName());
		gl.glShaderSource(fragid, 1, new String[]{temp}, new int[]{temp.length()},0);
		gl.glCompileShader(fragid);
		

		temp = loadFile(getVertexCodeName());
		gl.glShaderSource(vertid, 1, new String[]{temp}, new int[]{temp.length()},0);
		gl.glCompileShader(vertid);

		//Program
		id = gl.glCreateProgram();
		gl.glAttachShader(id, vertid);
		gl.glAttachShader(id, fragid);

		gl.glLinkProgram(id);
		Log.debug("Linking.");
		Renderer.assertNoError(gl);
		
		
		getLocations(gl);
		Log.debug("Locations.");
		Renderer.assertNoError(gl);
		
		logProgram(gl,id,getName());
		Renderer.assertNoError(gl);
		
		//Without this, uniforms aren't set correctly.
		gl.glUseProgram(id);
		setUniforms(gl);
		Log.debug("Uniforms.");
		Renderer.assertNoError(gl);
		
		
		gl.glUseProgram(0);
	}
	
	/**
	 * Since the locations exposed by shaders vary depending on the shader, we do this.
	 * @param gl
	 */
	public abstract void getLocations(GL2 gl);
	
	public abstract void setUniforms(GL2 gl);
	
	abstract String getFragmentCodeName();
	abstract String getVertexCodeName();
	
	
	
	/**
	 * Get the name of the shader.
	 */
	public abstract String getName();
	
	private String loadFile(String fname){
		
		try{
			
			RandomAccessFile f = Filesystem.get(fname);
			FileInputStream fis = new FileInputStream(f.getFD());
			BufferedReader bsr = new BufferedReader(new InputStreamReader(fis));
			
			StringBuilder sb = new StringBuilder();

			String line;
			try {
				while ((line = bsr.readLine()) != null){
					sb.append(line);
					sb.append('\n');
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}

		catch(FileNotFoundException ex){
			Log.warn("Unable to find the shader's code.");
			return "";
		}
		catch(Exception ex){
			Log.warn("Shader "+getName()+" failed to load!");
			return "";
		}
		
	}
	
	
	
	private final void logProgram(final GL2 gl, final int programId, final String name)
	{
		final IntBuffer lenBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGetProgramiv(programId, GL2.GL_INFO_LOG_LENGTH, lenBuffer);
		final int length = lenBuffer.get();

		if (length > 1)
		{
			lenBuffer.clear();
			final ByteBuffer logBuffer = Buffers.newDirectByteBuffer(length);
			gl.glGetProgramInfoLog(programId, length, lenBuffer, logBuffer);
			final byte[] logData = new byte[length];
			logBuffer.get(logData);
			String message = new String(logData);
			if(!message.contains("Validation successful")){
				Log.err("Error in program "+name+": "+new String(logData));
				checkVertLogInfo(gl);
				checkFragLogInfo(gl);
			}
		}
		else
		{
			Log.info("Shader " + name + " completed. Logs follow.");
			checkVertLogInfo(gl);
			checkFragLogInfo(gl);
		}
	}
	
	private void checkVertLogInfo(GL2 gl)  
	{
		IntBuffer iVal = Buffers.newDirectIntBuffer(1);
		gl.glGetShaderiv(vertid, GL2.GL_INFO_LOG_LENGTH, iVal);
		int length = iVal.get();
		if (length <= 1){return;}
		ByteBuffer infoLog = Buffers.newDirectByteBuffer(length);
		iVal.flip();
		gl.glGetShaderInfoLog(vertid, length, iVal, infoLog);
		byte[] infoBytes = new byte[length];
		infoLog.get(infoBytes);String msg = new String(infoBytes);
		if(msg.contains("successful")){//FIXME This might be phrased differently on other drivers.
			if(msg.toLowerCase().contains("warning")){Log.warn("GLSL Vertex warning : " + msg);}}
		else{
			Log.err("Vertex log error : "+msg);
		}
		Log.info("Vert: "+msg);
	}
	private void checkFragLogInfo(GL2 gl)  
	{
		IntBuffer iVal = Buffers.newDirectIntBuffer(1);
		gl.glGetShaderiv(fragid, GL2.GL_INFO_LOG_LENGTH, iVal);
		int length = iVal.get();
		if (length <= 1){return;}
		ByteBuffer infoLog = Buffers.newDirectByteBuffer(length);
		iVal.flip();
		gl.glGetShaderInfoLog(fragid, length, iVal, infoLog);
		byte[] infoBytes = new byte[length];
		infoLog.get(infoBytes);
		String msg = new String(infoBytes);
		if(msg.contains("successful")){//FIXME This might be phrased differently on other drivers.
			if(msg.toLowerCase().contains("warning")){Log.warn("GLSL Fragment warning : " + msg);}}
			else{
				Log.err("Fragment log error : "+msg);
			}
		Log.info("Frag: "+msg);
	}

	public void unload(GL2 gl) {
		/*Ensure not in use*/
		gl.glUseProgram(0);
		gl.glDetachShader(id, vertid);
		gl.glDetachShader(id,fragid);
		/* Delete the shaders */
		gl.glDeleteShader(vertid);
		gl.glDeleteShader(fragid);
		/* Delete the shader object */
		gl.glDeleteProgram(id);
	}

}
