package net.ember.logging;

public class Log {

	public static void info(String string) {
		System.out.println("[INFO]: "+string);
	}

	public static void warn(String string) {
		System.err.println("[WARN]: "+string);
	}

	public static void err(String string) {
		System.err.println("[ERR]: "+string);
	}

	public static void debug(String string) {
		System.out.println("[DEBUG]: "+string);
	}

}
