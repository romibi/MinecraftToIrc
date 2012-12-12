package ch.romibi.minecraft.toIrc;

import java.io.IOException;


public class McToIrc {
	
	public static IrcHandler irc;
	public static McHandler mcThread;
	public static ConsoleHandler console;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		mcThread = new McHandler();
		mcThread.start();
		irc = new IrcHandler();
		console = new ConsoleHandler();
		console.start();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		programmLoop();
	}

	private static void programmLoop() {
		
		boolean exit = false;
		while(!exit) {
			if(!mcThread.isAlive()) {
				exit = true;
				System.out.println("mc-Server stopped");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		irc.stop();
		try {
			console.consoleReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendToIrc(String string) {
		if(irc!=null) {
			irc.send(string);
		}
	}
	
	public static void sendCommandToMc(String string) {
		mcThread.sendToMc(string);
	}
	
	public static void sendToMc(String string) {
		mcThread.sendToMc("me IRC: "+string);
	}

	public static void sendToIrcAsUser(String user, String msg) {
		if(irc!=null) {
			irc.sayAsUser(user, msg);
		}
	}

	public static void userLoggedIn(String user) {
		if(irc != null) {
			irc.userLoggedIn(user);
		}
	}

	public static void userLoggedOut(String user) {
		irc.userLoggedOut(user);
	}

}
