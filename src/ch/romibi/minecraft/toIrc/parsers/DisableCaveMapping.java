package ch.romibi.minecraft.toIrc.parsers;

import java.util.HashMap;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;

public class DisableCaveMapping extends AbstractKeywordEvent {

	private String keyword = "discave";
	public String user; 
	
	public DisableCaveMapping() {
		super.setKeyword(keyword);
	}

	
	@Override
	protected void doEvent(MessageEvent me) {	//TODO: whisper to McUser directly after login event
		user = me.getNick();
		EnableCaveMapping.users.put(user, false);
		me.getSession().sayPrivate(user, "Cave-Mapping will be disabled after relog in Minecraft");
		McToIrc.sendCommandToMc("tell "+user+" Cave-Mapping will be disabled after relog");
	}

}