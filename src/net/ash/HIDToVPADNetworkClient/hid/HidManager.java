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
package net.ash.HIDToVPADNetworkClient.hid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.hid.hid4java.Hid4JavaHidManagerBackend;
import net.ash.HIDToVPADNetworkClient.hid.purejavahid.PureJavaHidManagerBackend;
import net.ash.HIDToVPADNetworkClient.util.Settings;

@Log
public class HidManager {
    private final static HidManagerBackend backend;

    public static HidDevice getDeviceByPath(String path) throws IOException {
        return backend.getDeviceByPath(path);
    }
    
    public static List<HidDevice> getAttachedControllers() {
        List<HidDevice> connectedGamepads = new ArrayList<HidDevice>();

        for (HidDevice info : backend.enumerateDevices()) {
            if (isGamepad(info)) {
                // Skip Xbox controller under windows. We should use XInput instead.
                if (isXboxController(info) && Settings.isWindows()) {
                    continue;
                }
                connectedGamepads.add(info);
            }
        }
        return connectedGamepads;
    }

    public static boolean isGamepad(HidDevice info) {
        if (info == null) return false;
        short usage = info.getUsage();
        return (usage == 0x05 || usage == 0x04 || isNintendoController(info) || isPlaystationController(info));
    }

    private static boolean isPlaystationController(HidDevice info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x054c);
    }

    private static boolean isNintendoController(HidDevice info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x57e);
    }

    private static boolean isXboxController(HidDevice info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x045e) && ((info.getProductId() == 0x02ff) || (info.getProductId() == 0x02a1));
    }

    static {
        if (Settings.isMacOSX()) {
            backend = new Hid4JavaHidManagerBackend();
        } else if (Settings.isWindows()) {
            backend = new PureJavaHidManagerBackend();
        } else if (Settings.isLinux()) {
            backend = new Hid4JavaHidManagerBackend();
        } else{
            backend = null;
        }
        log.info("Plattform: " + System.getProperty("os.name"));
        if(backend != null){          
           log.info("Backend: " + backend.getClass().getSimpleName());
        }else{
            log.info("No Backend loaded =(");
        }
    }
}
