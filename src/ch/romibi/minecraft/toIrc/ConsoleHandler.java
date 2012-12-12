package ch.romibi.minecraft.toIrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHandler extends Thread {
	
	public BufferedReader consoleReader;

	
	public void run() {
		consoleReader = new BufferedReader(new InputStreamReader(System.in));
		String consoleInputCache = "";
		
		boolean exit = false;
		while (!exit) {
			// ReadConsole
			try {

				if(consoleReader.ready()) {
					consoleInputCache = consoleReader.readLine();
					
					if(consoleInputCache != null) {
						parseInputFromConsole(consoleInputCache.trim());
					}
				}
			} catch (IOException e) {
				exit = true;
				//e.printStackTrace();
			}
			
			// Sleep
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void parseInputFromConsole(String string) {
		McToIrc.mcThread.sendToMc(string);
	}
}
