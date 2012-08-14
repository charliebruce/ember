package net.ember.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.ember.filesystem.Filesystem;
import net.ember.graphics.Material;
import net.ember.logging.Log;

public class MaterialFileLoader {

	/**
	 * Parse a Material Descriptor File and create a set of Materials which are stored until unload.
	 * @param file
	 */
	public static void parse(RandomAccessFile file){
		
		
		BufferedReader inreader;
		
		try {
			inreader = new BufferedReader(new FileReader(file.getFD()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String line;
		Material m = null;
		boolean onMaterial = false;
		try {
			while((line = inreader.readLine())!=null){
				/*
				 * Remove comments and blank lines, remove excess whitespace.
				 */
				if(line.startsWith("//")) continue;
				line=line.replaceAll(" {2,}", " ");
				if(line.startsWith(" ")){
					line = line.replaceFirst(" ", "");
				}
				if(line.equals(" ")||line.equals("")) continue;
				if(line.contains("{")&&onMaterial){
					Log.warn("Malformed material file - in a material but up another layer. Layerception not permissible!");
					continue;
				}
				if(line.contains("}")&&!onMaterial){
					Log.warn("Malformed material file - closing a material without being on one!");
					continue;
				}
				
				
				if(line.contains("{")){
					String name = line.replace("{", "").replaceAll(" ", "");
					Log.info("Importing material "+name);
					m = new Material(name);
					onMaterial=true;
					continue;
				}
				
				if(line.contains("}")){
					Material.put(m);
					onMaterial = false;
					continue;
				}
				
				if(line.startsWith("diffusemap ")){
					m.albedoMap = line.replace("diffusemap ", "").replaceAll(" ", "");
					continue;
				}
				if(line.startsWith("normalmap ")){
					m.normalMap = line.replace("normalmap ", "").replaceAll(" ", "");
					continue;
				}
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//bla bla bla
		
		//Material.put(lol);
		
	}
}
