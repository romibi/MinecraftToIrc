package ch.romibi.minecraft.toIrc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.*;
import jerklib.events.IRCEvent.*;
import jerklib.listeners.IRCEventListener;
import ch.romibi.minecraft.toIrc.enums.*;
import ch.romibi.minecraft.toIrc.interfaces.*;
import ch.romibi.minecraft.toIrc.parsers.*;

public class IrcHandler implements IRCEventListener {

	private String nick;
	private String channel;
	private String server;
	private String usersuffix;
	private int trimNicksAt;

	private ConnectionManager botManager;
	private Session botSession;

	private Map<String, Session> sessions;
	private Map<String, ConnectionManager> managers;
	private Map<String, String> usernameMappings;


	public IrcHandler() {
		nick = McToIrc.configFile.getProperty("nick");
		channel = "#" + McToIrc.configFile.getProperty("channel");
		server = McToIrc.configFile.getProperty("server");
		usersuffix = McToIrc.configFile.getProperty("usersuffix");
		try {
			trimNicksAt = Integer.parseInt(McToIrc.configFile.getProperty("trimNicksAt"));
		} catch (NumberFormatException e1) {
			trimNicksAt = 6;
		}
		publicParsers = new ArrayList<MessageParser>();
		privateParsers = new ArrayList<MessageParser>();
		
		//Public Parsers
		//TODO: Add more Public Parsers
		
		//Private Parsers
		privateParsers.add(new EnableCaveMapping());
		privateParsers.add(new DisableCaveMapping());
		//TODO: Add more Private Parsers
		

		/*
		 * ConnectionManager takes a Profile to use for new connections.
		 */
		botManager = new ConnectionManager(new Profile(nick));

		/*
		 * One instance of ConnectionManager can connect to many IRC networks.
		 * ConnectionManager#requestConnection(String) will return a Session object.
		 * The Session is the main way users will interact with this library and IRC
		 * networks
		 */
		botSession = botManager.requestConnection(server);

		/*
		 * JerkLib fires IRCEvents to notify users of the lib of incoming events
		 * from a connected IRC server.
		 */
		botSession.addIRCEventListener(this);
		
	}

	@Override
	public void receiveEvent(IRCEvent e) {
		if (e.getType() == Type.CONNECT_COMPLETE) {
			e.getSession().join(channel);
		} else if (e.getType() == Type.CHANNEL_MESSAGE) {
			MessageEvent me = (MessageEvent) e;
			if(e.getSession().equals(botSession) && sessions != null && !sessions.containsKey(me.getNick())){
				parseChannelMessage(me);
				McToIrc.sendToMc("<"+me.getNick() + "> " + me.getMessage());
			}
		} else if (e.getType() == Type.PRIVATE_MESSAGE) {
			MessageEvent me = (MessageEvent) e;
			if(e.getSession().equals(botSession)) {
				parseBotRequeset(me);
			} else {
				McToIrc.sendCommandToMc("tell "+convertUsernameToMc(me.getSession().getNick())+" "+me.getNick()+" from IRC whispers: "+me.getMessage());
			}
		} else if (e.getType() == Type.JOIN_COMPLETE && e.getSession().equals(botSession)) {
			JoinCompleteEvent jce = (JoinCompleteEvent) e;
			jce.getChannel().say("MC-Server is Starting");
		} else {
			System.out.println(e.getType() + " " + e.getRawEventData());
		}

	}
	
	private void parseChannelMessage(MessageEvent me) {
		for (MessageParser parser : publicParsers) {
			parser.parse(me);
		}
	}

	private void parseBotRequeset(MessageEvent me) {
		for (MessageParser parser : privateParsers) {
			parser.parse(me);
		}
	}

	public void send(String string) {
		if (botSession != null && botSession.getChannel(channel) != null) {
			botSession.sayChannel(botSession.getChannel(channel), string);
		}
	}

	public Session getSessionForUser(String user) {
		user = convertUsernameToIrc(user);
		if (sessions == null) {
			sessions = new HashMap<String, Session>();
		}
		if (sessions.get(user) != null) {
			return sessions.get(user);
		} else {
			ConnectionManager tempmanager;
			if (managers == null) {
				managers = new HashMap<String, ConnectionManager>();
			}
			if (managers.get(user) != null) {
				tempmanager = managers.get(user);
			} else {
				tempmanager = new ConnectionManager(new Profile(user));
				managers.put(user, tempmanager);
			}
			Session tempsession = tempmanager.requestConnection(server);
			tempsession.addIRCEventListener(this);
			sessions.put(user, tempsession);
			return tempsession;
		}
	}

	public void userLoggedIn(String user) {
		getSessionForUser(user);
	}

	public void sayAsUser(String user, String msg) {
		getSessionForUser(user).sayChannel(getSessionForUser(user).getChannel(channel), msg);
	}

	public void userLoggedOut(String user) {
		logoutUser(user);
	}

	private void logoutUser(String user) {
		sessions.get(convertUsernameToIrc(user)).close("logged out");
		sessions.remove(convertUsernameToIrc(user));
	}

	public String convertUsernameToIrc(String username) {
		if (usernameMappings == null) {
			usernameMappings = new HashMap<String, String>();
		}
		if (usernameMappings.containsKey(username)) {
			return usernameMappings.get(username);
		} else {
			usernameMappings.put(username, username.substring(0, trimNicksAt) + usersuffix);
			return username.substring(0, trimNicksAt) + usersuffix;
		}
	}

	public String convertUsernameToMc(String username) {
		if (usernameMappings == null) {
			return null;
		}
		if (usernameMappings.containsValue(username)) {
			for (Entry<String, String> entry : usernameMappings.entrySet()) {
				if (entry.getValue().equals(username)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	public void stop() {
		if (sessions != null && sessions.values() != null) {
			for (Session session : sessions.values()) {
				session.close("Stopping Minecraft-Server");
			}
		}
		sessions = null;
		botSession.close("Stopping Minecraft-Server");
		botSession = null;

		if (managers != null && managers.values() != null) {
			for (ConnectionManager manager : managers.values()) {
				manager.quit();
			}
		}

		botManager.quit();
	}

}
