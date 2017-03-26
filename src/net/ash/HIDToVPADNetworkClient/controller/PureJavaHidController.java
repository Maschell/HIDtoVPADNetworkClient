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
import java.util.Arrays;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.PureJavaHidApiManager;
import purejavahidapi.HidDevice;
import purejavahidapi.InputReportListener;

public class PureJavaHidController extends Controller implements InputReportListener {
    public static Controller getInstance(String deviceIdentifier) throws IOException, ControllerInitializationFailedException {
        HidDevice device = PureJavaHidApiManager.getDeviceByPath(deviceIdentifier);
        // We use a special version to optimize the data for the switch pro controller
        if (device.getHidDeviceInfo().getVendorId() == SwitchProController.SWITCH_PRO_CONTROLLER_VID
                && device.getHidDeviceInfo().getProductId() == SwitchProController.SWITCH_PRO_CONTROLLER_PID) {
            return new SwitchProController(deviceIdentifier);
        } else if (device.getHidDeviceInfo().getVendorId() == DS4NewController.DS4_NEW_CONTROLLER_VID
                && device.getHidDeviceInfo().getProductId() == DS4NewController.DS4_NEW_CONTROLLER_PID) {
            return new DS4NewController(deviceIdentifier);
        } else {
            return new PureJavaHidController(deviceIdentifier);
        }
    }

    public PureJavaHidController(String identifier) throws ControllerInitializationFailedException {
        super(ControllerType.PureJAVAHid, identifier);
    }

    private Object dataLock = new Object();
    protected byte[] currentData = new byte[1];

    protected int PACKET_LENGTH = 64;

    @Getter @Setter(AccessLevel.PRIVATE) private HidDevice hidDevice;

    @Override
    public boolean initController(String identifier) {
        HidDevice device;
        try {
            device = PureJavaHidApiManager.getDeviceByPath(identifier);

            device.setInputReportListener(this);
            setHidDevice(device);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    @Synchronized("dataLock")
    public byte[] pollLatestData() {
        return currentData.clone();
    }

    @Override
    public void destroyDriver() {
        getHidDevice().close();
    }

    @Override
    public short getVID() {
        return getHidDevice().getHidDeviceInfo().getVendorId();
    }

    @Override
    public short getPID() {
        return getHidDevice().getHidDeviceInfo().getProductId();
    }

    @Override
    @Synchronized("dataLock")
    public void onInputReport(HidDevice source, byte reportID, byte[] reportData, int reportLength) {
        if (isActive()) {
            int length = PACKET_LENGTH;
            if (reportLength < length) {
                length = reportLength;
            }
            currentData = Arrays.copyOfRange(reportData, 0, length);
        }
    }

    @Override
    public String getInfoText() {
        // TODO:
        if (getVID() == 0x57e) {
            if (getPID() == 0x2006) {
                return "Joy-Con (L) on " + getIdentifier();
            } else if (getPID() == 0x2007) {
                return "Joy-Con (R) on " + getIdentifier();
            }
        }

        return "USB HID on " + getIdentifier();
    }
}