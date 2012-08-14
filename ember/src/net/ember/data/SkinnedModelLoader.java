package net.ember.data;

import java.io.FileInputStream;

import net.ember.logging.Log;

public class SkinnedModelLoader {

	public static Model get(FileInputStream gis, int[] params, String name) {
		// TODO Auto-generated method stub
		Log.warn("Not yet implemented Skinned Models");
		SkinnedModel m = new SkinnedModel();
		m.name=name;
		return m;
	}

}
