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
package net.ash.HIDToVPADNetworkClient.controller;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.hid.HidDevice;
import net.ash.HIDToVPADNetworkClient.hid.HidManager;

@Log
public class HidController extends Controller {
    @Getter @Setter(AccessLevel.PRIVATE) private HidDevice hidDevice;

    public static Controller getInstance(String deviceIdentifier) throws IOException, ControllerInitializationFailedException {

        HidDevice device = HidManager.getDeviceByPath(deviceIdentifier);

        short vid = 0;
        short pid = 0;
        if (device != null) {
            vid = device.getVendorId();
            pid = device.getProductId();
        }

        // We use a special version to optimize the data for the switch pro controller
        if (vid == SwitchProController.SWITCH_PRO_CONTROLLER_VID && pid == SwitchProController.SWITCH_PRO_CONTROLLER_PID) {
            return new SwitchProController(deviceIdentifier);
        } else if (vid == DS4NewController.DS4_NEW_CONTROLLER_VID && pid == DS4NewController.DS4_NEW_CONTROLLER_PID) {
            return new DS4NewController(deviceIdentifier);
        } else {
            return new HidController(deviceIdentifier);
        }
    }

    public HidController(String identifier) throws ControllerInitializationFailedException {
        super(ControllerType.HIDController, identifier);
    }

    @Override
    public boolean initController(String identifier) {
        try {
            HidDevice device = HidManager.getDeviceByPath(identifier);

            if (device == null || !device.open()) {
                return false;
            }
            log.info("HidDevice opened!");

            setHidDevice(device);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] pollLatestData() {
        byte[] result = hidDevice.getLatestData();
        return result;
    }

    @Override
    public void destroyDriver() {
        try {
            getHidDevice().close();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("device not open")) {
                log.info("Error closing the device." + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Override
    public short getVID() {
        return getHidDevice().getVendorId();
    }

    @Override
    public short getPID() {
        return getHidDevice().getProductId();
    }

    @Override
    public String getInfoText() {
        // TODO: own class for joycons
        if (getVID() == 0x57e) {
            if (getPID() == 0x2006) {
                return "Joy-Con (L) (0x057e:0x2006) on " + getIdentifier();
            } else if (getPID() == 0x2007) {
                return "Joy-Con (R) (0x057e:0x2007) on " + getIdentifier();
            }
        }

        String name = getHidDevice().getProductString();
        return String.format("%s (0x%04X:0x%04X) on %s", (name != null) ? name : "USB HID", getVID(), getPID(), getIdentifier());
    }
}