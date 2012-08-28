package net.ember.logging;

public class Log {

	public static void info(String string) {
		System.out.print("[INFO]: "+string+"\n");
		/*try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//*/
	}

	public static void warn(String string) {
		System.err.print("[WARN]: "+string+"\n");
		
	}

	public static void err(String string) {
		System.err.print("[ERR]: "+string+"\n");
	}

	public static void debug(String string) {
		System.out.print("[DEBUG]: "+string+"\n");
		/*try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//*/
	}

	public static void close() {
		// TODO Auto-generated method stub
		
	}

}
