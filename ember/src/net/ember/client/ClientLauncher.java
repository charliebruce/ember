package net.ember.client;

public class ClientLauncher {

	public static void main(String[] args){
		Client.init();
		Client.run();
		Client.shutdown();
	}
}
