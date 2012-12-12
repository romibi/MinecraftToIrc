package ch.romibi.minecraft.toIrc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class McHandler extends Thread{
	
	private static Process mcProcess;
	private BufferedWriter mcWriter;

	public McHandler() {
		startMc();
	}
	
	public void run () {
		BufferedReader mcReader = new BufferedReader(new InputStreamReader(mcProcess.getErrorStream()));
		String mcOutputCache = "";
	
		mcWriter = new BufferedWriter(new OutputStreamWriter(mcProcess.getOutputStream()));
		boolean exit = false;
		
		while (!exit) {
			
			try {
				mcOutputCache = mcReader.readLine();
				if(mcOutputCache != null) {
					parseOutputFromMc(mcOutputCache.trim());
				}
				try {
					if(mcProcess.exitValue() == 0) {
						System.out.println(mcProcess.exitValue());
						exit = true;
					}else {
						System.out.println(mcProcess.exitValue());
					}
				} catch (IllegalThreadStateException e) {
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void startMc() {
		ProcessBuilder pb = new ProcessBuilder("java", "-Xmx1024M", "-Xms1024M", "-jar", "minecraft_server.jar", "nogui");
		//TODO: move arguments to config
		try {
			mcProcess = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void parseOutputFromMc(String cache) {
		Pattern userMsgPattern = Pattern.compile(".*\\[INFO]\\W<(.*)\\>\\W(.*)");
		Matcher userMsg = userMsgPattern.matcher(cache);
		if(userMsg.matches()) {
			String user = userMsg.group(1);
			String msg = userMsg.group(2);

			McToIrc.sendToIrcAsUser(user, msg);
		}
		
		Pattern userLoggedInPattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d\\-\\d\\d\\W\\d\\d:\\d\\d\\:\\d\\d\\W\\[.*\\]\\W(.*)\\[.*\\]\\Wlogged\\ in.*"); //TODO: make better regex
		Matcher userLoggedIn = userLoggedInPattern.matcher(cache);
		if(userLoggedIn.matches()){
			String user = userLoggedIn.group(1);
			McToIrc.userLoggedIn(user);
		}
		
		Pattern userLoggedOutPattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d\\-\\d\\d\\W\\d\\d:\\d\\d\\:\\d\\d\\W\\[.*\\]\\W(.*)\\Wlost\\ connection.*"); //TODO: make better regex
		Matcher userLoggedOut = userLoggedOutPattern.matcher(cache);
		if(userLoggedOut.matches()){
			String user = userLoggedOut.group(1);
			McToIrc.userLoggedOut(user);
		}
		
		System.out.println(cache);
	}
	
	public void sendToMc(String string) {
		System.out.println("Writing "+string);
		try {
			mcWriter.append(string);
			mcWriter.newLine();
			mcWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
