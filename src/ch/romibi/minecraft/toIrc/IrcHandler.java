package ch.romibi.minecraft.toIrc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private UserDisplayMode userDisplayMode = UserDisplayMode.SINGLE;

	private ConnectionManager botManager;
	private Session botSession;

	private Map<String, Session> sessions;
	private Map<String, ConnectionManager> managers;
	private Map<String, String> usernameMappings;

	private Map<MessageParser, List<AccessType>> parsers = new HashMap<MessageParser, List<AccessType>>();

	//private List<MessageParser> publicParsers;
	//private List<MessageParser> privateParsers;

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
		
		if (McToIrc.configFile.getProperty("userDisplayMode").toUpperCase().equals("SINGLE")) {
			userDisplayMode = UserDisplayMode.SINGLE;
		} else if(McToIrc.configFile.getProperty("userDisplayMode").toUpperCase().equals("MULTI")) {
			userDisplayMode = UserDisplayMode.MULTI;
		} else if(McToIrc.configFile.getProperty("userDisplayMode").toUpperCase().equals("MULTIOP")) {
			userDisplayMode = UserDisplayMode.MULTIOP;
		}
		
		//Parsers
//		registerParser(new EnableCaveMapping(), AccessType.PRIVATE, AccessType.OP);
//		registerParser(new DisableCaveMapping(), AccessType.PRIVATE, AccessType.OP);
		registerParser(new EnableCaveMapping(), AccessType.PRIVATE);
		registerParser(new DisableCaveMapping(), AccessType.PRIVATE);
		//TODO: Add more Parsers
		
		

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
	
// Parser things

	public void registerParser(MessageParser parser, AccessType... neededAccessTypes) {
		ArrayList<AccessType> accesstypeslist = new ArrayList<AccessType>();
		for (AccessType accessType : neededAccessTypes) {
			accesstypeslist.add(accessType);
		}
		parsers.put(parser, accesstypeslist);
	}

	public void parseIfAccess(IRCEvent e) {
		Iterator<Entry<MessageParser, List<AccessType>>> it = parsers.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<MessageParser, List<AccessType>> entry = (Entry<MessageParser, List<AccessType>>) it.next();

			ArrayList<AccessType> list = getAviableAccessTypesFromIRCEvent(e);
			
			if (list.containsAll(entry.getValue())) {
				entry.getKey().parse(e);
			}

			//it.remove(); // avoids a ConcurrentModificationException
		}
	}

	private ArrayList<AccessType> getAviableAccessTypesFromIRCEvent(IRCEvent e) {
		ArrayList<AccessType> list = new ArrayList<AccessType>();
		
		if(e.getType() == Type.CHANNEL_MESSAGE) {
			list.add(AccessType.PUBLIC);
		}
		
		if(e.getType() == Type.PRIVATE_MESSAGE) {
			list.add(AccessType.PRIVATE);
		}
		
		if(getNickFromIRCEvent(e) != null && convertUsernameToMc(getNickFromIRCEvent(e)) == null) {
			list.add(AccessType.IRC);
		} else if(getNickFromIRCEvent(e) != null){
			list.add(AccessType.MC);
		}
		
		if(getNickFromIRCEvent(e) != null && McToIrc.isOp(getNickFromIRCEvent(e))) {
			list.add(AccessType.OP);
		}
		return list;
	}
	
// Event things

	@Override
	public void receiveEvent(IRCEvent e) {
		parseIfAccess(e);
		if (e.getType() == Type.CONNECT_COMPLETE) {
			e.getSession().join(channel);
		} else if (e.getType() == Type.CHANNEL_MESSAGE) {
			MessageEvent me = (MessageEvent) e;
			if (e.getSession().equals(botSession) && sessions != null && !sessions.containsKey(me.getNick())) {
				McToIrc.sendToMc("<" + me.getNick() + "> " + me.getMessage());
			}
		} else if (e.getType() == Type.PRIVATE_MESSAGE) {
			MessageEvent me = (MessageEvent) e;
			if (!e.getSession().equals(botSession)) {
				McToIrc.sendCommandToMc("tell " + convertUsernameToMc(me.getSession().getNick()) + " " + me.getNick() + " from IRC whispers: " + me.getMessage());
			}
		} else if (e.getType() == Type.JOIN_COMPLETE && e.getSession().equals(botSession)) {
			JoinCompleteEvent jce = (JoinCompleteEvent) e;
			jce.getChannel().say("MC-Server is Starting");
		} else if(e.getSession().equals(botSession) && e.getType() == Type.JOIN && convertUsernameToMc(getNickFromIRCEvent(e)) == null){
			McToIrc.sendToMc("User "+((JoinEvent)e).getNick()+" joined Chat!");
		} else if(e.getSession().equals(botSession) && (e.getType() == Type.QUIT || e.getType() == Type.PART) && convertUsernameToMc(getNickFromIRCEvent(e)) == null){
			McToIrc.sendToMc("User "+getNickFromIRCEvent(e)+" left Chat!");
		} else {
			System.out.println("IRC: "+e.getType() + " " + e.getRawEventData());
		}

	}
	
	public void userLoggedIn(String mcUser) {
		loginUser(mcUser);
	}

	public void userLoggedOut(String mcUser) {
		logoutUser(mcUser);
	}

// Action things as mcUser (Don't Forget to check for userDisplayMode)
	
	private void loginUser(String mcUser) {
		if (userDisplayMode == UserDisplayMode.SINGLE || (userDisplayMode == UserDisplayMode.MULTIOP && !McToIrc.isOp(mcUser))) {
			send(mcUser +" logged in.");
		} else if(userDisplayMode == UserDisplayMode.MULTI || (userDisplayMode == UserDisplayMode.MULTIOP && McToIrc.isOp(mcUser))) {
			getSessionForUser(mcUser);
		}
	}

	private void logoutUser(String mcUser) {
		if (userDisplayMode == UserDisplayMode.SINGLE || (userDisplayMode == UserDisplayMode.MULTIOP && !McToIrc.isOp(mcUser))) {
			send(mcUser +" logged out.");
		} else if(userDisplayMode == UserDisplayMode.MULTI || (userDisplayMode == UserDisplayMode.MULTIOP && McToIrc.isOp(mcUser))) {
			sessions.get(convertUsernameToIrc(mcUser)).close("logged out");
			sessions.remove(convertUsernameToIrc(mcUser));
		}
	}
	
	public void sayAsUser(String mcUser, String msg) {
		if (userDisplayMode == UserDisplayMode.SINGLE || (userDisplayMode == UserDisplayMode.MULTIOP && !McToIrc.isOp(mcUser))) {
			send("<"+mcUser+"> : "+msg);
		} else if(userDisplayMode == UserDisplayMode.MULTI || (userDisplayMode == UserDisplayMode.MULTIOP && McToIrc.isOp(mcUser))) {
			getSessionForUser(mcUser).sayChannel(getSessionForUser(mcUser).getChannel(channel), msg);
		}
	}
	
	public void sendAsUser(String mcUser, String string) {
		if (userDisplayMode == UserDisplayMode.SINGLE || (userDisplayMode == UserDisplayMode.MULTIOP && !McToIrc.isOp(mcUser))) {
			if(string.substring(0, 1).equals("/")) {
				emulateIrcCommandForEmulatedUser(mcUser, string);
			} else {
				System.err.println("Dear Programmer. Please use \"sayAsUser(String mcUser, String msg)\" if it's not a command!");
			}
		} else if(userDisplayMode == UserDisplayMode.MULTI || (userDisplayMode == UserDisplayMode.MULTIOP && McToIrc.isOp(mcUser))) {
			getSessionForUser(mcUser).sayChannel(getSessionForUser(mcUser).getChannel(channel), string);
		}
	}
	
// Actions

	public void send(String string) {
		if (botSession != null && botSession.getChannel(channel) != null) {
			botSession.sayChannel(botSession.getChannel(channel), string);
		}
	}
	
	private void emulateIrcCommandForEmulatedUser(String mcUser, String string) {
		String[] words = string.split(" ");
		if(words[0].equals("/me")) {
			send("* "+mcUser+" "+string.substring(4));
		} else { //TODO: emulate more Commands
			System.err.println("Unrecognized Command to emulate: "+string);
		}
	}

// Nick / User things

	private String getNickFromIRCEvent(IRCEvent e) {
		switch (e.getType()) {
			case PRIVATE_MESSAGE:
			case CHANNEL_MESSAGE:
				return ((MessageEvent) e).getNick();
			case QUIT:
				return ((QuitEvent) e).getNick();
			case PART:
				return ((PartEvent) e).getWho();
			case JOIN:
				return ((JoinEvent) e).getNick();
			case KICK_EVENT:
				return ((KickEvent) e).getWho();
			case AWAY_EVENT:
				return ((AwayEvent) e).getNick();
			default:
				return null;
		}
	}

	public Session getSessionForUser(String mcUser) {
		String user = convertUsernameToIrc(mcUser);
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

	public String convertUsernameToIrc(String mcUsername) {
		if (usernameMappings == null) {
			usernameMappings = new HashMap<String, String>();
		}
		if (usernameMappings.containsKey(mcUsername)) {
			return usernameMappings.get(mcUsername);
		} else {
			String trimmedUsername;
			if(mcUsername.length()>trimNicksAt) { //TODO: Test if +/- 1 ...
				trimmedUsername = mcUsername.substring(0, trimNicksAt);
			} else {
				trimmedUsername = mcUsername;
			}
			usernameMappings.put(mcUsername, trimmedUsername + usersuffix);
			return trimmedUsername + usersuffix;
		}
	}

	public String convertUsernameToMc(String ircUsername) {
		if (usernameMappings == null) {
			return null;
		}
		if (usernameMappings.containsValue(ircUsername)) {
			for (Entry<String, String> entry : usernameMappings.entrySet()) {
				if (entry.getValue().equals(ircUsername)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
// Other things

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
