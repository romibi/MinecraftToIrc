package ch.romibi.minecraft.toIrc.parsers;

import jerklib.events.MessageEvent;
import ch.romibi.minecraft.toIrc.interfaces.MessageParser;

public abstract class AbstractKeywordEvent implements MessageParser {
	protected String keyword;
	
	@Override
	public void parse(MessageEvent me) {
		if(me.getMessage().toLowerCase().equals("."+keyword.toLowerCase())) {
			doEvent(me);
		}
	}
	
	protected abstract void doEvent(MessageEvent me);

}
