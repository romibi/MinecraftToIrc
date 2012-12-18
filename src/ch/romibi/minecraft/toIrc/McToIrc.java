package ch.romibi.minecraft.toIrc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.romibi.minecraft.toIrc.enums.EventType;
import ch.romibi.minecraft.toIrc.interfaces.EventHandler;
import ch.romibi.minecraft.toIrc.interfaces.ParameteredRunnable;


public class McToIrc {
	
	private static IrcHandler irc;
	private static McHandler mcThread;
	private static ConsoleHandler console;
	public static Properties configFile = new Properties();
	

	
	private static List<EventHandler> loggonListener = new ArrayList<EventHandler>();
	private static List<EventHandler> loggoutListener = new ArrayList<EventHandler>();
	private static List<EventHandler> messageListener = new ArrayList<EventHandler>();
	private static List<EventHandler> meActionListener = new ArrayList<EventHandler>();
	private static List<EventHandler> userUsedUnknownCommandListener = new ArrayList<EventHandler>();
	private static List<EventHandler> userTeleportedListener = new ArrayList<EventHandler>();

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
		console.setConsoleTextEnteredListener(new ParameteredRunnable<String>() {
			
			@Override
			public void run(String param) {
				McToIrc.mcThread.sendToMc(param);
			}
		});
		console.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if(McToIrc.mcThread.isAlive()) {
					McToIrc.sendCommandToMc("stop");
				}
				McToIrc.irc.stop();
				try {
					McToIrc.console.consoleReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
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

//	public static void sendToIrc(String string) {
//		if(irc!=null) {
//			irc.send(string);
//		}
//	}
	
	public static void sendCommandToMc(String string) {
		mcThread.sendToMc(string);
	}
	
	public static void sendToMc(String string) {
		mcThread.sendToMc("me IRC: "+string);
	}

//	public static void sendToIrcAsUser(String user, String msg) {
//		if(irc!=null) {
//			irc.sayAsUser(user, msg);
//		}
//	}
	
	
	// Register EventHandlers
	
	public static void registerLogginListener(EventHandler handler) {
		loggonListener.add(handler);
	}
	
	public static void registerLoggoutListener(EventHandler handler) {
		loggoutListener.add(handler);
	}

	public static void registerMessageListener(EventHandler handler) {
		messageListener.add(handler);
	}

	public static void registerMeActionListener(EventHandler handler) {
		meActionListener.add(handler);
	}

	public static void registerUserUsedCommandListener(EventHandler handler) {
		userUsedUnknownCommandListener.add(handler);
	}

	public static void registerUserTeleportedListener(EventHandler handler) {
		userTeleportedListener.add(handler);
	}
	
	//Notify EventHandlers

	public static void userLoggedIn(String user) {
		if(irc != null) {
			irc.userLoggedIn(user); //TODO Do As EventHandler...
			for (EventHandler handler : loggonListener) {
				handler.eventAction(EventType.USER_LOGGED_IN, user);
			}
		}
	}

	public static void userLoggedOut(String user) {
		irc.userLoggedOut(user); //TODO Do as EventHandler
		for (EventHandler handler : loggoutListener) {
			handler.eventAction(EventType.USER_LOGGED_OUT, user);
		}
	}

	public static void userMessage(String user, String msg) {
		irc.sayAsUser(user, msg); //TODO Do as EventHandler
		for (EventHandler handler : messageListener) {
			handler.eventAction(EventType.USER_MESSAGE, msg);
		}
	}
	
	public static void userMeAction(String user, String msg) {
		irc.sendAsUser(user, "/me "+msg); //TODO Do as EventHandler
		for (EventHandler handler : meActionListener) {
			handler.eventAction(EventType.USER_ME_ACTION, msg);
		}
	}
	
	public static void userUsedUnknownCommand(String user, String command) {
		for (EventHandler handler : userUsedUnknownCommandListener) {
			handler.eventAction(EventType.USER_USED_UNKNOWN_COMMAND, command);
		}
	}
	
	public static void userTeleported(String user, Integer x, Integer y, Integer z) {
		for (EventHandler handler : userUsedUnknownCommandListener) {
			handler.eventAction(EventType.USER_TELEPORTED, x, y, z);
		}
	}

	public static boolean isOp(String nick) {
			// TODO Check if User is OP
		return false;
	}

}
