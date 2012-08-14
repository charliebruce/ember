package net.ember.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import net.ember.data.MaterialFileLoader;
import net.ember.data.Model;
import net.ember.data.ModelLoader;
import net.ember.filesystem.Filesystem;
import net.ember.game.Region;
import net.ember.game.World;
import net.ember.graphics.Graphics;
import net.ember.graphics.Material;
import net.ember.logging.Log;

/**
 * Implements background loading of resources which aren't immediately required, such as physics meshes and textures for as-of-yet inactive regions.
 * @author Charlie
 *
 */
public class LoadManager implements Runnable {

	boolean abort=false;
	private Object lock = new Object();

	private boolean regionNeedsLoad = false;
	private int region = 0;
	private int buffer = 0;

	private List<Material> materialsToLoad = new ArrayList<Material>();
	private List<Model> modelsToLoad = new ArrayList<Model>();
	
	@Override
	public void run() {

		while(!abort){

			if(regionNeedsLoad){//If some work needs doing
				synchronized(lock){
					
					Log.info("Loading region " + region);
				
					World.regions[buffer]=new Region(region);
					World.regions[buffer].load();//For testing. For real use, instead inform LoadManager about this change, display load screen till then.


					try {
						MaterialFileLoader.parse(Filesystem.get("nonfree/doom.mmd"));
						MaterialFileLoader.parse(Filesystem.get("materials/materials.mmd"));
						MaterialFileLoader.parse(Filesystem.get("regions/0/materials.mmd"));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					/**
					 * Get any resources which are required and load them as quickly as possible.
					 * load() will have informed us of the resources required.
					 */
					synchronized(materialsToLoad){
						materialsToLoad.add(Material.get("hellknight"));
						//materialsToLoad.add(Material.get("vase_round"));
						
					}
					
					try {
						modelsToLoad.add(ModelLoader.get(Filesystem.get("test/unitsphere.mesh"), "unitsphere"));
						modelsToLoad.add(ModelLoader.get(Filesystem.get("test/hellknight.mesh"), "hellknight"));
						modelsToLoad.add(World.regions[0].regionGraphicsMesh);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				
					/**
					 * Go through the materials-to-load list, load the texture, and make it available to the 
					 * "to-load-in-spare-time" list for the renderer.
					 */

					/**
					 * Likewise for model geometry, except instead of going through the Materials.materials list, we
					 * go straight through into Models in renderer once loaded.
					 */

				

					Log.info("Finished load of region "+region);
					regionNeedsLoad=false;
				}
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	public void delayedLoadModels(List<String> toAdd){

	}

	

	public void stop() {
		abort=true;
	}


	public void loadRegion(int regionid,int bufferid) {
		synchronized(lock){
			regionNeedsLoad = true;
			region = regionid;
			buffer = bufferid;
		}
	}


	public boolean idle() {
		return !regionNeedsLoad;
	}


	public void loadNumMaterials(int num, GL3 gl){

		synchronized(materialsToLoad)
		{
			Iterator<Material> i = materialsToLoad.iterator();
			int count=0;
			while(i.hasNext()&&count<num){
				Material m = i.next();
				if (m.onGraphics){
					i.remove();
					continue;
				}
				Graphics.renderer.loadMaterial(m, gl);
				i.remove();
				count++;
			}
		}//TODO also load the texturedata file separately from the rest...
	}
	
	
	public void loadAllMaterials(GL3 gl){
		synchronized(materialsToLoad)
		{
			for(Material m: materialsToLoad){
				if(m==null)
				{
					Log.warn("Material pointed to does not exist!");
					continue;
				}
				if(!m.onGraphics)
				Graphics.renderer.loadMaterial(m, gl);
			}
			materialsToLoad.clear();
		}//TODO also load the texturedata file separately from the rest...
	}


	public void loadAllModels(GL3 gl) {
		synchronized(modelsToLoad)
		{
			for(Model m: modelsToLoad){
				if(!m.onGraphics)
					Graphics.renderer.loadModel(m, gl);
			}
			modelsToLoad.clear();
		}
	}

	/**
	 * This is called every tick from the renderer - it should only do work when vital, to minimise stalls.
	 * @param gl
	 */
	public void loadData(GL3 gl) {
		loadAllMaterials(gl);
		loadAllModels(gl);
		Graphics.renderer.loadData=false;
	}


	//TODO this
	public void loadMaterial(Material material) {
		synchronized(materialsToLoad){
			materialsToLoad.add(material);
			Graphics.renderer.loadData=true;
		}
	}
}
