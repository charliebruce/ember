package net.ember.graphics.shaders;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

/**
 * A shader which transforms and projects static mesh geometry.
 * It DOES NOT expects to see X,Y,Z,S,T,NX,NY,NZ,TX,TY,TZ,TW 
 * @author Peter
 *
 */
public class StaticMeshShaderNormalMapped extends Shader {

	/**
	 * Vertex Attributes - inputs.
	 */
	public int vertexLocation;
	public int normalLocation;
	public int texcoordLocation;
	public int tangentLocation;
	
	/**
	 * Vertex Uniforms - parameters.
	 */
	public int mvpLocation;
	public int normalmatrixLocation;
	
	/**
	 * Fragment Uniforms.
	 */
	public int normalmapLocation;
	public int albedomapLocation;
	public int alphamapLocation;
	public int specularmapLocation;
	
	
	@Override
	public void getLocations(GL2 gl) {
		
		/**
		 * Vertex
		 */
		vertexLocation = gl.glGetAttribLocation(id, "vertexin");
		normalLocation = gl.glGetAttribLocation(id, "normalin");
		texcoordLocation = gl.glGetAttribLocation(id, "texcoordin");
		tangentLocation = gl.glGetAttribLocation(id, "tangentin");
		
		mvpLocation = gl.glGetUniformLocation(id, "mvp");
		normalmatrixLocation = gl.glGetUniformLocation(id, "normalmatrix");
		
		/**
		 * Fragment
		 */
		albedomapLocation = gl.glGetUniformLocation(id, "albedomap");
		normalmapLocation = gl.glGetUniformLocation(id, "normalmap");
		alphamapLocation = gl.glGetUniformLocation(id, "alphamap");
		specularmapLocation = gl.glGetUniformLocation(id, "specularmap");
		

	}
	
	@Override
	public void setUniforms(GL2 gl) {
		gl.glUniform1i(albedomapLocation, 0);
		gl.glUniform1i(normalmapLocation, 1);
		gl.glUniform1i(alphamapLocation, 2);
		gl.glUniform1i(specularmapLocation, 3);
		//gl.glUseProgram(0);
	}
	@Override
	String getFragmentCodeName() {
		return "shaders/deferred/staticmesh.fp";
	}

	@Override
	String getVertexCodeName() {
		return "shaders/deferred/staticmesh.vp";
	}

	@Override
	public String getName() {
		return "Normal-Mapped Static Mesh Shader"; 
	}

	

	
}
