package ch.romibi.minecraft.toIrc.parsers;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.McToIrc;
import ch.romibi.minecraft.toIrc.interfaces.MessageParser;

public abstract class AbstractKeywordEvent implements MessageParser {
	
	private String keyword;
	
	public void setKeyword(String newKeyword) {
		keyword = newKeyword;
	}
	
	@Override
	public void parse(MessageEvent me) {
<<<<<<< HEAD
		if(me.getMessage().toLowerCase().equals("."+keyword.toLowerCase())) {
=======
		if (me.getMessage().toLowerCase().equals(McToIrc.configFile.getProperty("botCommandMarker")+keyword)) {
>>>>>>> Moved Message Patterns to Config File & fixed KeywordEvent
			doEvent(me);
		}
	}
	
	protected abstract void doEvent(MessageEvent me);

}
