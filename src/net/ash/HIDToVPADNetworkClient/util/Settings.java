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
import lombok.Setter;
import lombok.extern.java.Log;

//TODO autosave IP addr

@Log
public class Settings {
	private static final String CONFIG_FILE_NAME = "hidtovpad.properties";
	
    public static final int DETECT_CONTROLLER_INTERVAL = 1000;
    public static final int HANDLE_INPUTS_INTERVAL = 15;
    public static final int MAXIMUM_TRIES_FOR_RECONNECTING = 10;
    public static final int SLEEP_AFER_POLLING = 10;
    public static final int SENDING_CMD_SLEEP_IF_NOT_CONNECTED = 500;
    public static final int PING_INTERVAL = 1000;
    public static final int PROCESS_CMD_INTERVAL = 10; 
    
	@Getter @Setter
	private static String ipAddr = "192.168.0.35"; //@Maschell, you're welcome

    private Settings() {}

    public static void loadSettings() {
    	File configDir = new File(getConfigDir());
    	if (!configDir.exists()) {
    		log.info("Creating " + configDir.getAbsolutePath() + "...");
    		configDir.mkdirs();
    	}
    	File configFile = getConfigFile();
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
			return;
		}
    	
    	Settings.ipAddr = prop.getProperty("ipAddr");
    	
    	log.info("Loaded config successfully!");
    }
    
    private static File getConfigFile() {
        return new File(getConfigDir() + CONFIG_FILE_NAME);
    }

    public static void saveSettings() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            log.info("Settings saved.");
            saveSettings(configFile);
        }
    }
    
    private static void saveSettings(File configFile) {
    	Properties prop = new Properties();
    	
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
        return "config/";
    }
}
