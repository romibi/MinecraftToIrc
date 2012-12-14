package ch.romibi.minecraft.toIrc.parsers;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;
import ch.romibi.minecraft.toIrc.interfaces.MessageParser;

public abstract class AbstractKeywordMethod implements MessageParser {

	private String keyword;

	public void setKeyword(String newKeyword) {
		keyword = newKeyword;
	}

	@Override
	public void parse(MessageEvent me) {
		String[] words = me.getMessage().split("\\ ",2);

		if (words[0].toLowerCase().equals(McToIrc.configFile.getProperty("botCommandMarker") + keyword)) {
			doEvent(me);
		}
	}

	protected abstract void doEvent(MessageEvent me);

}
