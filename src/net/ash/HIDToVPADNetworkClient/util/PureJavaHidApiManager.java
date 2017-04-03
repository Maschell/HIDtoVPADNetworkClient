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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.PureJavaHidApi;

public final class PureJavaHidApiManager {

    private PureJavaHidApiManager() {
    }

    /**
     * Searches the corresponding HIDDevice for the given path
     * 
     * @param path
     *            Path of the HIDDevice
     * @return It the device is found, it will be returned. Otherwise null is returned.
     * @throws IOException
     */
    public static HidDevice getDeviceByPath(String path) throws IOException {
        HidDeviceInfo deviceinfo = ControllerManager.getDeviceInfoByPath(path);
        if (deviceinfo != null) {
            HidDevice result = PureJavaHidApi.openDevice(deviceinfo);
            if (result != null) {
                return result;
            }
        }
        /*
         * List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices(); HidDevice result = null; for (HidDeviceInfo info : devList) { String real_path =
         * info.getPath(); if (real_path.equals(path)) { return PureJavaHidApi.openDevice(info); } }
         */
        return null;
    }

    public static List<HidDeviceInfo> getAttachedController() {
        List<HidDeviceInfo> connectedGamepads = new ArrayList<HidDeviceInfo>();

        for (HidDeviceInfo info : PureJavaHidApi.enumerateDevices()) {
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

    public static boolean isGamepad(HidDeviceInfo info) {
        if (info == null) return false;
        short usagePage = info.getUsagePage();
        return (usagePage == 0x05 || usagePage == 0x01 || usagePage == 0x04 || isNintendoController(info) || isPlaystationController(info));
    }

    private static boolean isPlaystationController(HidDeviceInfo info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x054c);
    }

    private static boolean isNintendoController(HidDeviceInfo info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x57e);
    }

    private static boolean isXboxController(HidDeviceInfo info) {
        if (info == null) return false;
        return (info.getVendorId() == 0x045e) && ((info.getProductId() == 0x02ff) || (info.getProductId() == 0x02a1));
    }
}
