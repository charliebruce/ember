package net.ember.logging;
/**
 * Measures the time taken for events in a frame.
 * Very basic but should be fast.
 * Could support events, sub-events and variable numbers of events with enough time/care.
 * @author Administrator
 *
 */
public class Timer {

	
	public static final int numEvents = 5;

	public static final int Display = 0;
	public static final int Input = 1;
	public static final int Physics = 2;
	public static final int World = 3;
	public static final int Sound = 4;

	
	public static long[][] log;
	static int length = 1024;
	public static int atIndex=0;
	
	
	public static void init(){
		log = new long[length][numEvents];
	}

	
	public static void newframe(){
		atIndex++;
		if(atIndex==length){
			//TODO dump to a performance analysis file, along with important events?
			atIndex=0;
		}
	}
	public static void event(int eventId, long timetaken) {
		log[atIndex][eventId]=timetaken;//TODO check bounds?
	}


	public static String breakdownCurrentFrame() {
		return "Disp: "+ms(log[atIndex][0])+" Inp: "+ms(log[atIndex][1])+" Phys: "+ms(log[atIndex][2])+" Wrld: "+ms(log[atIndex][3])+" Snd: "+ms(log[atIndex][4]);
	}


	private static String ms(long l) {
		return ((float)l/1000000f)+"ms";
	}


	public static void close() {
		// TODO Save to file?
		
	}

}
