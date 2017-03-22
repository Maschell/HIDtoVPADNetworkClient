/*******************************************************************************
 * Copyright (c) 2017 Ash (QuarkTheAwesome) & Maschell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package net.ash.HIDToVPADNetworkClient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.java.Log;

//TODO autosave IP addr

@Log
public class Settings {
	private static final String CONFIG_FILE_NAME = "config.properties";
	
	@Getter
    private static int detectControllerInterval = 1000;
	@Getter
	private static int handleInputsInterval = 15;
	@Getter
    private static int maxTriesForReconnecting = 10;
	@Getter
    private static int sleepAfterPolling = 10;
	/**
	 * What does this even mean?
	 */
	@Getter
    private static int sendingCmdSleepIfNotConnected = 500;
	@Getter
    private static int pingInterval = 1000;
	@Getter
    private static int processCmdInterval = 10;
	@Getter
	private static String ipAddr = "192.168.0.35"; //@Maschell, you're welcome

    private Settings() {}

    public static void loadSettings() {
    	File configDir = new File(getConfigDir());
    	if (!configDir.exists()) {
    		log.info("Creating " + configDir.getAbsolutePath() + "...");
    		configDir.mkdirs();
    	}
    	File configFile = new File(getConfigDir() + CONFIG_FILE_NAME);
    	if (!configFile.exists()) {
    		log.info("Creating " + configFile.getAbsolutePath() + " with default values...");
    		try {
				configFile.createNewFile();
			} catch (IOException e) {
				log.severe("Could not create config file!");
				e.printStackTrace();
				log.warning("Using default values");
			}
    		saveSettings(configFile);
    		return;
    	}
    	
    	log.info("Loading config from " + configFile.getAbsolutePath() + "...");
    	
    	Properties prop = new Properties();
    	try {
			prop.load(new FileInputStream(configFile));
		} catch (IOException e) {
			log.severe("Error while loading config file!");
			e.printStackTrace();
			log.warning("Using default values");
		}
    	
    	String s_detectControllerInterval = prop.getProperty("detectControllerInterval");
    	String s_handleInputsInterval = prop.getProperty("handleInputsInterval");
    	String s_maxTriesForReconnecting = prop.getProperty("maxTriesForReconnecting");
    	String s_sleepAfterPolling = prop.getProperty("sleepAfterPolling");
    	String s_sendingCmdSleepIfNotConnected = prop.getProperty("sendingCmdSleepIfNotConnected");
    	String s_pingInterval = prop.getProperty("pingInterval");
    	String s_processCmdInterval = prop.getProperty("processCmdInterval");
    	String s_ipAddr = prop.getProperty("ipAddr");
    	
    	int detectControllerInterval, handleInputsInterval, maxTriesForReconnecting, sleepAfterPolling, sendingCmdSleepIfNotConnected, pingInterval, processCmdInterval;
    	
    	try {
    		detectControllerInterval = Integer.parseInt(s_detectControllerInterval);
    		handleInputsInterval = Integer.parseInt(s_handleInputsInterval);
    		maxTriesForReconnecting = Integer.parseInt(s_maxTriesForReconnecting);
    		sleepAfterPolling = Integer.parseInt(s_sleepAfterPolling);
    		sendingCmdSleepIfNotConnected = Integer.parseInt(s_sendingCmdSleepIfNotConnected);
    		pingInterval = Integer.parseInt(s_pingInterval);
    		processCmdInterval = Integer.parseInt(s_processCmdInterval);
    	} catch (NumberFormatException e) {
    		log.warning("Config file contains invalid values!");
    		log.warning("Reconstructing...");
    		saveSettings(configFile);
    		return;
    	}
    	
    	Settings.detectControllerInterval = detectControllerInterval;
    	Settings.handleInputsInterval = handleInputsInterval;
    	Settings.maxTriesForReconnecting = maxTriesForReconnecting;
    	Settings.sleepAfterPolling = sleepAfterPolling;
    	Settings.sendingCmdSleepIfNotConnected = sendingCmdSleepIfNotConnected;
    	Settings.pingInterval = pingInterval;
    	Settings.processCmdInterval = processCmdInterval;
    	Settings.ipAddr = s_ipAddr;
    	
    	log.info("Loaded config successfully!");
    }
    
    private static void saveSettings(File configFile) {
    	Properties prop = new Properties();
    	prop.setProperty("detectControllerInterval", Integer.toString(Settings.detectControllerInterval));
		prop.setProperty("handleInputsInterval", Integer.toString(Settings.handleInputsInterval));
		prop.setProperty("maxTriesForReconnecting", Integer.toString(Settings.maxTriesForReconnecting));
		prop.setProperty("sleepAfterPolling", Integer.toString(Settings.sleepAfterPolling));
		prop.setProperty("sendingCmdSleepIfNotConnected", Integer.toString(Settings.sendingCmdSleepIfNotConnected));
		prop.setProperty("pingInterval", Integer.toString(Settings.pingInterval));
		prop.setProperty("processCmdInterval", Integer.toString(Settings.processCmdInterval));
		prop.setProperty("ipAddr", Settings.ipAddr);
		
		try {
			FileOutputStream outStream = new FileOutputStream(configFile);
			prop.store(outStream, "HIDToVPADNetworkClient");
			outStream.close();
		} catch (FileNotFoundException e) {
			log.severe("Could not open the new config file!");
			e.printStackTrace();
			log.warning("New file will not be written.");
			return;
		} catch (IOException e) {
			log.severe("Could not write the new config file!");
			e.printStackTrace();
			log.warning("New file will not be written.");
			return;
		}
    }
    
    private static String getConfigDir() {
    	String os = System.getProperty("os.name");
    	if (os.contains("Windows")) {
    		return System.getenv("APPDATA") + "/HIDToVPADNetworkClient/";
    	} else if (os.contains("Mac OS X")) {
    		return System.getProperty("user.home") + "/Library/Application Support/HIDToVPADNetworkClient/";
    	} else { //Linux
    		return System.getProperty("user.home") + "/.config/HIDToVPADNetworkClient/";
    	}
    }
}
