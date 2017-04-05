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
import java.util.List;

import net.ash.HIDToVPADNetworkClient.hid.hid4java.Hid4JavaHidManagerBackend;
import net.ash.HIDToVPADNetworkClient.hid.purejavahid.PureJavaHidManagerBackend;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public class HidManager {
    private final static HidManagerBackend backend;

    public static List<HidDeviceInfo> getAttachedController() {
        return backend.getAttachedController();
    }

    public static HidDevice getDeviceByPath(String path) throws IOException {
        return backend.getDeviceByPath(path);
    }

    static {
        if (Settings.isMacOSX()) {
            backend = new Hid4JavaHidManagerBackend();
        } else if (Settings.isWindows()) {
            backend = new PureJavaHidManagerBackend();
        } else if (Settings.isLinux()) {
            backend = new Hid4JavaHidManagerBackend();
        } else
            backend = null;
    }
}
