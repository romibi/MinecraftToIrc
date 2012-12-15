package ch.romibi.minecraft.toIrc.parsers;

import java.util.HashMap;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;
import ch.romibi.minecraft.toIrc.enums.EventType;
import ch.romibi.minecraft.toIrc.interfaces.EventHandler;

public class EnableCaveMapping extends AbstractKeywordEvent implements EventHandler {

	protected String keyword = "encave";
	public String user; 
	public static HashMap<String, Boolean> users = new HashMap<String, Boolean>();
	
	public EnableCaveMapping() {
		super.setKeyword(keyword);
		McToIrc.registerLogginListener(this);
	}

	
	@Override
	protected void doEvent(MessageEvent me) {
		user = me.getNick();
		users.put(user, true);
		me.getSession().sayPrivate(user, "Cave-Mapping will be enabled after relog in Minecraft");
		McToIrc.sendCommandToMc("tell "+user+" Cave-Mapping will be enabled after relog");
	}


	@Override
	public void eventAction(EventType event, Object... args) {
		String user = (String) args[0];
		
		if (users.get(user) == null) {
			System.err.println("new user: "+user);
			users.put(user, false);
		} else if (EnableCaveMapping.users.get(user) == true) {
			System.err.println("enabled cavemapping");
			McToIrc.sendCommandToMc("tell "+user+" �0�0�1�e�f");
		}
	}

}
