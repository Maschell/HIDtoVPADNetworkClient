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
package net.ash.HIDToVPADNetworkClient.hid.purejavahid;

import java.io.IOException;
import java.util.Arrays;

import lombok.Synchronized;
import net.ash.HIDToVPADNetworkClient.hid.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;

class PureJavaHidDevice implements HidDevice, InputReportListener {
    private purejavahidapi.HidDevice myDevice = null;
    private final purejavahidapi.HidDeviceInfo myDeviceInfo;

    private final Object dataLock = new Object();
    protected byte[] currentData = new byte[0];

    public PureJavaHidDevice(HidDeviceInfo info) {
        this.myDeviceInfo = info;
    }

    @Override
    @Synchronized("dataLock")
    public void onInputReport(purejavahidapi.HidDevice source, byte reportID, byte[] reportData, int reportLength) {
        currentData = Arrays.copyOfRange(reportData, 0, reportLength);
    }

    @Override
    public short getVendorId() {
        return myDeviceInfo.getVendorId();
    }

    @Override
    public short getProductId() {
        return myDeviceInfo.getProductId();
    }

    @Override
    public boolean open() {
        boolean result = true;
        try {
            myDevice = purejavahidapi.PureJavaHidApi.openDevice(myDeviceInfo);
            myDevice.setInputReportListener(this);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {
        myDevice.close();
    }

    @Override
    @Synchronized("dataLock")
    public byte[] getLatestData() {
        return currentData.clone();
    }

    @Override
    public short getUsagePage() {
        return myDeviceInfo.getUsagePage();
    }
    
    @Override
    public short getUsageID() {
        return myDeviceInfo.getUsageID();
    }

    @Override
    public String getPath() {
        return myDeviceInfo.getPath();
    }

    @Override
    public String toString() {
        return "PureJavaHidDevice [vid= " + String.format("%04X", getVendorId()) + ", pid= " + String.format("%04X", getProductId()) + ", path= " + getPath().trim()
                + ", usage= " + String.format("%04X:%04X", getUsagePage(), getUsageID()) + ", data=" + Arrays.toString(currentData) + "]";
    }
}
