package ch.romibi.minecraft.toIrc.parsers;

import java.util.HashMap;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;

public class EnableCaveMapping extends AbstractKeywordEvent {

	private String keyword = "encave";
	public String user; 
	public static HashMap<String, Boolean> users = new HashMap<String, Boolean>();
	
	public EnableCaveMapping() {
		super.setKeyword(keyword);
	}

	
	@Override
	protected void doEvent(MessageEvent me) {
		user = me.getNick();
		users.put(user, true);
		me.getSession().sayPrivate(user, "Cave-Mapping will be enabled after relog in Minecraft");
		McToIrc.sendCommandToMc("tell "+user+" Cave-Mapping will be enabled after relog");
	}

}
