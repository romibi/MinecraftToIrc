package ch.romibi.minecraft.toIrc.parsers;

import java.util.List;

import jerklib.events.IRCEvent;
import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;
import ch.romibi.minecraft.toIrc.interfaces.MessageParser;

public abstract class AbstractKeywordEvent implements MessageParser {
	
	private String keyword;
	
	public void setKeyword(String newKeyword) {
		keyword = newKeyword;
	}
	
	@Override
	public void parse(IRCEvent e) {
		if (e instanceof MessageEvent && ((MessageEvent)e).getMessage().toLowerCase().equals(McToIrc.configFile.getProperty("botCommandMarker")+keyword.toLowerCase())) {
			doEvent((MessageEvent) e);
		}
	}
	
	protected abstract void doEvent(MessageEvent me);
	
	public void addToParserList(List<MessageParser> list) {
		list.add(this);
	}

}
