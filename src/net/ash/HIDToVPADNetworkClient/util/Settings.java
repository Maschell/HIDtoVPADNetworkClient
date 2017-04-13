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
import java.util.Arrays;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
public final class Settings {
    private static final String CONFIG_FILE_NAME = "hidtovpad.properties";

    public static final int DETECT_CONTROLLER_INTERVAL = 1000;
    public static final int HANDLE_INPUTS_INTERVAL = 15;
    public static final int MAXIMUM_TRIES_FOR_RECONNECTING = 10;
    public static final int SLEEP_AFER_POLLING = 10;
    public static final int SENDING_CMD_SLEEP_IF_NOT_CONNECTED = 500;
    public static final int PING_INTERVAL = 1000;
    public static final int PROCESS_CMD_INTERVAL = 10;

    public static final int DETECT_CONTROLLER_ACTIVE_INTERVAL = 100;

    public static final int RUMBLE_STRENGTH = 50; // in % TODO: Create setting for this.

    public static boolean SCAN_AUTOMATICALLY_FOR_CONTROLLERS = true;

    public static boolean DEBUG_UDP_OUTPUT = false;
    public static boolean DEBUG_TCP_PING_PONG = false;
    public static boolean SEND_DATA_ONLY_ON_CHANGE = false;
    public static boolean AUTO_ACTIVATE_CONTROLLER = false;

    @Getter @Setter private static String ipAddr = "192.168.0.35"; // @Maschell, you're welcome

    private Settings() {
    }

    public static void loadSettings() {
        File configDir = new File(getConfigDir());
        if (!configDir.exists()) {
            log.info("Creating " + configDir.getAbsolutePath() + "...");
            configDir.mkdirs();
        }
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            ControllerFiltering.setDefaultFilterStates();
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
        String autoActivatingControllerString = prop.getProperty("autoActivatingController");
        String sendDataOnlyOnChanges = prop.getProperty("sendDataOnlyOnChanges");
        String scanAutomaticallyForControllers = prop.getProperty("scanAutomaticallyForControllers");

        if (autoActivatingControllerString != null) { // We don't combine the if statements to keep the default value.
            if ("true".equals(autoActivatingControllerString)) {
                Settings.AUTO_ACTIVATE_CONTROLLER = true;
            } else {
                Settings.AUTO_ACTIVATE_CONTROLLER = false;
            }
        }
        if (sendDataOnlyOnChanges != null) { // We don't combine the if statements to keep the default value.
            if ("true".equals(sendDataOnlyOnChanges)) {
                Settings.SEND_DATA_ONLY_ON_CHANGE = true;
            } else {
                Settings.SEND_DATA_ONLY_ON_CHANGE = false;
            }
        }
        if (scanAutomaticallyForControllers != null) { // We don't combine the if statements to keep the default value.
            if ("true".equals(scanAutomaticallyForControllers)) {
                Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS = true;
            } else {
                Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS = false;
            }
        }

        String filterStates = prop.getProperty("filterStates");
        if (filterStates != null) {
            ControllerFiltering.loadFilterStates(filterStates);
        } else {
            ControllerFiltering.setDefaultFilterStates();
        }

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
        prop.setProperty("autoActivatingController", Boolean.toString(Settings.AUTO_ACTIVATE_CONTROLLER));
        prop.setProperty("sendDataOnlyOnChanges", Boolean.toString(Settings.SEND_DATA_ONLY_ON_CHANGE));
        prop.setProperty("scanAutomaticallyForControllers", Boolean.toString(Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS));
        prop.setProperty("filterStates", ControllerFiltering.getFilterStates());

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

    public static boolean isLinux() {
        return getPlattform() == Platform.LINUX;
    }

    public static boolean isWindows() {
        return getPlattform() == Platform.WINDOWS;
    }

    public static boolean isMacOSX() {
        return getPlattform() == Platform.MAC_OS_X;
    }

    public static Platform getPlattform() {
        String os = System.getProperty("os.name");

        if (os.contains("Linux")) {
            return Platform.LINUX;
        } else if (os.contains("Windows")) {
            return Platform.WINDOWS;
        } else if (os.contains("Mac OS X")) {
            return Platform.MAC_OS_X;
        }
        return Platform.UNKNOWN;
    }

    public enum Platform {
        LINUX(0x1), WINDOWS(0x2), MAC_OS_X(0x4), UNKNOWN(0x8);

        private int mask;

        private Platform(int mask) {
            this.mask = mask;
        }
    }

    // TODO rename this to something less nonsensical
    public static class ControllerFiltering {
        public static enum Type {
            HIDGAMEPAD(0, "HID Gamepads", Platform.LINUX.mask | Platform.WINDOWS.mask | Platform.MAC_OS_X.mask),
            XINPUT(5, "XInput controllers", Platform.WINDOWS.mask),
            HIDKEYBOARD(1, "HID Keyboards", Platform.LINUX.mask | Platform.MAC_OS_X.mask),
            HIDMOUSE(2, "HID Mice", Platform.LINUX.mask),
            HIDOTHER(3, "Other HIDs", Platform.LINUX.mask | Platform.WINDOWS.mask | Platform.MAC_OS_X.mask),
            LINUX(4, "Linux controllers", Platform.LINUX.mask),;

            private int index;
            @Getter private String name;
            private int platforms;

            private Type(int index, String name, int platforms) {
                this.index = index;
                this.name = name;
                this.platforms = platforms;
            }

            public boolean isSupportedOnPlatform() {
                return (platforms & getPlattform().mask) != 0;
            }
        }

        private static boolean[] filterStates = new boolean[Type.values().length];

        public static String getFilterStates() {
            return Arrays.toString(filterStates);
        }

        public static void loadFilterStates(String newFilterStates) {
            boolean[] newFilterStatesParsed = Utilities.stringToBoolArray(newFilterStates);
            if (newFilterStatesParsed.length != filterStates.length) {
                // TODO handle changes in filtering more gracefully
                log.warning("Number of controller filters in config does not match reality, using defaults...");
                setDefaultFilterStates();
            } else {
                filterStates = newFilterStatesParsed;
            }
        }

        public static void setFilterState(Type filter, boolean state) {
            filterStates[filter.index] = state;
        }

        public static boolean getFilterState(Type filter) {
            return filterStates[filter.index];
        }

        public static void setDefaultFilterStates() {
            filterStates[Type.HIDGAMEPAD.index] = true;
            filterStates[Type.HIDKEYBOARD.index] = false;
            filterStates[Type.HIDMOUSE.index] = false;
            filterStates[Type.HIDOTHER.index] = false;
            filterStates[Type.LINUX.index] = true;
            filterStates[Type.XINPUT.index] = true;
        }
    }
}
