package net.ember.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import net.ember.logging.Log;

public class PhysicsMeshLoader {

	public static PhysicsMesh get(RandomAccessFile f){

		FileInputStream fis;
		PhysicsMesh m = new PhysicsMesh();

		try {

			fis = new FileInputStream(f.getFD());

			byte[] i = new byte[36];
			ByteBuffer loo = ByteBuffer.wrap(i);
			fis.read(i);
			loo.rewind();
			if(loo.getInt()!=Type.PhysicsMesh)
				Log.warn("Physics Mesh format not correct.");
			int flags = loo.getInt();
			int datalength = loo.getInt();
			int indexlength = loo.getInt();
			for(int j=0;j<5;j++){
				loo.getInt();//Waste the remaining 5 bytes.
			}
			if((datalength%3!=0)||(indexlength%3!=0)){
				Log.warn("Physics mesh contains an odd number of data or index points: "+datalength + " data points, "+indexlength+" indices.");
			}



			byte[] data = new byte[datalength * 4];
			fis.read( data, 0, (datalength * 4));
			m.dataBuffer = ByteBuffer.wrap(data);
			m.dataBuffer.rewind();


			byte[] indices = new byte[indexlength*4];
			m.indexBuffer = ByteBuffer.wrap(indices);
			fis.read(indices,0,(indexlength*4));
			m.indexBuffer.rewind();

			m.faces=indexlength/3;//Each face has 3 points, so 3 point indices.
			m.points=datalength/3;//Each point defines x,y,z

			


		} catch (IOException e) {
			Log.info("Encountered an IOException when reading static physics mesh!");
			e.printStackTrace();
			return null;
		}
		m.loaded=true;
		return m;
	}

}
