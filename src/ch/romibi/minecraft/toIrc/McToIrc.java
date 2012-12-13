package ch.romibi.minecraft.toIrc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


public class McToIrc {
	
	public static IrcHandler irc;
	public static McHandler mcThread;
	public static ConsoleHandler console;
	public static Properties configFile = new Properties();;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String configFileContent = getConfigFileContents();
			configFile.load(new StringReader(configFileContent.replace("\\", "\\\\")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		mcThread = new McHandler();
		mcThread.start();
		irc = new IrcHandler();
		console = new ConsoleHandler();
		console.start();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		programmLoop();
	}

	private static String getConfigFileContents() throws IOException {
		StringBuffer strContent = new StringBuffer("");
		FileInputStream fin = null;
		try {
			fin = new FileInputStream("config.properties");
			for (int ch=fin.read(); ch != -1; ch = fin.read()) {
				strContent.append((char)ch);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return strContent.toString();
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
