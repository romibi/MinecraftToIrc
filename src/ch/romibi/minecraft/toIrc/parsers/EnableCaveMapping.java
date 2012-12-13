package ch.romibi.minecraft.toIrc.parsers;

import ch.romibi.minecraft.toIrc.McToIrc;
import jerklib.events.MessageEvent;

public class EnableCaveMapping extends AbstractKeywordEvent {

	private String keyword = "encave";

	public EnableCaveMapping() {
		super.setKeyword(keyword);
	}

	
	@Override
	protected void doEvent(MessageEvent me) {
		System.out.println("trololol");
		if(McToIrc.irc.convertUsernameToMc(me.getNick()) == null) {
			me.getSession().sayPrivate(me.getNick(), "Sorry! This command is only aviable for Minecraft-Users!");
		} else {
			McToIrc.sendCommandToMc("tell "+McToIrc.irc.convertUsernameToMc(me.getNick())+" not yet implemented");
		}
	}

}
