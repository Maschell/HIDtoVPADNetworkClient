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
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.hid.HidDevice;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;
import net.ash.HIDToVPADNetworkClient.util.Utilities;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;

@Log
class PureJavaHidDevice implements HidDevice {
    private purejavahidapi.HidDevice myDevice = null;
    private final purejavahidapi.HidDeviceInfo myDeviceInfo;

    protected byte[] currentData = new byte[0];

    public PureJavaHidDevice(HidDeviceInfo info) {
        this.myDeviceInfo = info;
    }

    @Override
    public short getVendorId() {
        return myDeviceInfo.getVendorId();
    }

    @Override
    public short getProductId() {
        return myDeviceInfo.getProductId();
    }

    private static boolean hasShownUdevErrorMessage = false;

    @Override
    public boolean open() {
        boolean result = true;
        try {
            myDevice = purejavahidapi.PureJavaHidApi.openDevice(myDeviceInfo);
        } catch (IOException e) {
            result = false;
            if (e.getMessage().contains("errno 13") && Settings.isLinux()) {
                if (!hasShownUdevErrorMessage) {
                    hasShownUdevErrorMessage = true;
                    String msg = "Could not access HID devices.\nTo fix this, please add the following udev rule:\n\nKERNEL==\"hidraw*\", SUBSYSTEM==\"hidraw\", MODE=\"0664\", GROUP=\"plugdev\"\n\nThis will allow everyone to read from HIDs, and users in the group \"plugdev\" to read and write.\nIt's reccomended to add yourself to this group.\n\nHID To VPAD Network Client will be unable to use HIDs until this issue is resolved.";
                    MessageBoxManager.addMessageBox(new MessageBox(msg, MessageBox.MESSAGE_ERROR));
                    log.severe(msg);
                    e.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
       
        
        return result;
    }

    @Override
    public void close() {
        myDevice.close();
    }

    private  byte[] data = new byte[64];
    @Override
    public byte[] getLatestData() {       
        int length = hid_read(data);
        if (length <= 0) return new byte[0];
        return Arrays.copyOf(data, length);
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
    public String getProductString() {
        String result = myDeviceInfo.getProductString();
        if(result != null) result = result.trim();
        return result;
    }

    @Override
    public String toString() {
        return "PureJavaHidDevice [vid= " + String.format("%04X", getVendorId()) + ", pid= " + String.format("%04X", getProductId()) + ", path= "
                + getPath().trim() + ", usage= " + String.format("%04X:%04X", getUsagePage(), getUsageID()) + ", data=" + Arrays.toString(currentData) + "]";
    }

    @Override
    public int hid_write(byte[] data, int length, byte reportID){
        try{
            return myDevice.setOutputReport(reportID, data, length);
        }catch(IllegalStateException e){
            return -1;
        }
    }
    
    @Override
    public int hid_read(byte[] data) {
        return hid_read(data,0);
    }

    @Override
    public int hid_read(byte[] data, int timeoutMillis) {
        try{
            return myDevice.getInputReport(data,timeoutMillis);
        }catch(IllegalStateException e){
            return -1;
        }
    }
}
