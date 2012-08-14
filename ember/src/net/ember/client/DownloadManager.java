package net.ember.client;

import java.util.LinkedList;
import java.util.List;

import net.ember.logging.Log;

/**
 * Download Manager requests file(s)
 * Named by bit EXCLUDING Client.basedir
 * Priorities are 0-highest.
 * @author Charlie
 *
 */
public class DownloadManager implements Runnable {

	List<String> toDownload;
	List<String> toDownloadUrgent;
	boolean abort=false;
	
	/**
	 * Initialise the download manager
	 */
	public DownloadManager(){
		toDownload = new LinkedList<String>();
		toDownloadUrgent = new LinkedList<String>();
	}
	
	/*
	 * Adding files
	 */
	public void downloadFiles(List<String> files, int priority) {
		if(priority==0){synchronized(toDownloadUrgent){
			toDownloadUrgent.addAll(files);
		}}
		else{
		synchronized(toDownload){
		toDownload.addAll(files);
		}}
	}
	public void downloadFile(String toDownload2, int priority) {
		if(priority==0){synchronized(toDownloadUrgent){
			toDownloadUrgent.add(toDownload2);
		}}
		else{
		synchronized(toDownload){
		toDownload.add(toDownload2);
		}}
	}
	
	@Override
	public void run() {
		//done in client init();
		while(!abort){
			//TODO prioritise
			String s=getNextThing();
			while(s!=null&&!s.equals(""))
				{download(s);removeThing(s);s=getNextThing();}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void download(String s) {
		Log.debug("Downloading "+s);
		//TODO test download code.
	}
	public void finish() {
		abort=true;
	}

	public void removeFromUrgent(String s){
		synchronized(toDownloadUrgent){
			toDownloadUrgent.remove(s);
		}
	}
	public void removeFromQueue(String s){
		synchronized(toDownload){
			toDownload.remove(s);
		}
	}
	int lastPriority=0;//0 high 1 low
	public String getNextThing(){
		lastPriority = 0;
		String s = getNextUrgent();
		if(s==null||s.equals("")) {lastPriority=1; s = getNext();}
		return s;
	}
	public void removeThing(String s){
		if(lastPriority==0) removeFromUrgent(s); else removeFromQueue(s);
	}
	public String getNextUrgent(){
		synchronized(toDownloadUrgent){
			if(toDownloadUrgent.size()>0)
			return toDownloadUrgent.get(0);
			else return "";
		}
	}
	public String getNext(){
		synchronized(toDownload){
			if(toDownload.size()>0)
			return toDownload.get(0);
			else return "";
		}
	}

	/**
	 * This is "blocking" download - only to be called when the file is needed BEFORE PROCEEDING
	 * Using this will stall the engine!
	 * @param string
	 */
	public void downloadBlocking(String string) {
		download(string);
	}

	public void stop() {
		abort=true;
	}


}
