package ch.romibi.minecraft.toIrc.interfaces;

import java.util.List;

import jerklib.events.IRCEvent;

public interface MessageParser {
	public void parse(IRCEvent e);
	public void addToParserList(List<MessageParser> list);
}
