package ch.romibi.minecraft.toIrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.romibi.minecraft.toIrc.interfaces.ParameteredRunnable;

public class ConsoleHandler extends Thread {
	
	public BufferedReader consoleReader;
	private ParameteredRunnable<String> consoleTextEntered;

	public void setConsoleTextEnteredListener(ParameteredRunnable<String> r)
	{
		consoleTextEntered = r;
	}
	
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
			}
			
			// Sleep
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void parseInputFromConsole(String string) {
		consoleTextEntered.run(string);
	}
}
