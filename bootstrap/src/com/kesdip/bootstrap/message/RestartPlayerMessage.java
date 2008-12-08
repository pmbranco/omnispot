/*
 * Disclaimer:
 * Copyright 2008 - Ke.S.Di.P. E.P.E - All rights reserved.
 * eof Disclaimer
 */
package com.kesdip.bootstrap.message;

import java.io.File;

import org.apache.log4j.Logger;

import com.kesdip.bootstrap.Config;

/**
 * Encapsulates the handling of a restart player message from the server.
 * 
 * @author Pafsanias Ftakas
 */
public class RestartPlayerMessage implements Message {
	private static final Logger logger =
		Logger.getLogger(RestartPlayerMessage.class);
	
	private static Process playerProcess;
	
	/**
	 * Query the player process to see if it is alive.
	 * @return True iff the player is still alive.
	 */
	public static boolean isPlayerProcessAlive() {
		try {
			int exitValue = playerProcess.exitValue();
			logger.info("The player process has exited with status: " +
					exitValue);
			return false;
		} catch (IllegalThreadStateException itse) {
			return true;
		}
	}

	@Override
	public void process() throws Exception {
		// Destroy the old player process if it exists.
		if (playerProcess != null) {
			playerProcess.destroy();
		}
		
		// Set up the command line 
		String[] cmdArray = new String[4];
		cmdArray[0] = "java";
		cmdArray[1] = "-cp";
		cmdArray[2] = Config.getSingleton().getPlayerClasspath();
		cmdArray[3] = Config.getSingleton().getPlayerMainClass();
		String workingDir = Config.getSingleton().getPlayerWorkingDir();
		
		playerProcess = Runtime.getRuntime().exec(
				cmdArray, null, new File(workingDir));
	}

	@Override
	public String toMessageString() {
		return "[RestartPlayer]";
	}

}
