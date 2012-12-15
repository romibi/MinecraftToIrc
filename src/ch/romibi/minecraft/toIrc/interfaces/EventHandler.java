package ch.romibi.minecraft.toIrc.interfaces;

import ch.romibi.minecraft.toIrc.enums.EventType;

public interface EventHandler {
	public void eventAction(EventType event, Object... args);
}
