package ch.romibi.minecraft.toIrc.enums;

public enum EventType {
	USER_LOGGED_IN,		// args = user
	USER_LOGGED_OUT,		// args = user
	USER_MESSAGE,		// args = message
	USER_ME_ACTION,		// args = message
	USER_USED_UNKNOWN_COMMAND,	// args = command
	USER_TELEPORTED		//args = coordinates
}
