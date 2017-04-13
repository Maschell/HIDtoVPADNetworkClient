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
package net.ash.HIDToVPADNetworkClient.hid.hid4java;

import java.util.Arrays;

import net.ash.HIDToVPADNetworkClient.hid.HidDevice;

class Hid4JavaHidDevice implements HidDevice {
    private org.hid4java.HidDevice myDevice;

    private final byte[] data = new byte[64];

    public Hid4JavaHidDevice(org.hid4java.HidDevice device) {
        this.myDevice = device;
    }

    @Override
    public boolean open() {
        return myDevice.open();
    }

    @Override
    public void close() {
        myDevice.close();
    }

    @Override
    public short getVendorId() {
        return myDevice.getVendorId();
    }

    @Override
    public short getProductId() {
        return myDevice.getProductId();
    }

    @Override
    public byte[] getLatestData() {
        int length = myDevice.read(data);
        if (length <= 0) return new byte[0];
        return Arrays.copyOf(data, length);
    }

    @Override
    public String getPath() {
        return myDevice.getPath();
    }

    @Override
    public String getProductString() {
        return myDevice.getProduct();
    }

    @Override
    public String toString() {
        return "Hid4JavaHidDevice [vid= " + getVendorId() + ", pid= " + getProductId() + ", usage= " + String.format("%04X:%04X", getUsagePage(), getUsageID())
                + ", data=" + Arrays.toString(data) + "]";
    }

    @Override
    public short getUsageID() {
        return (short) myDevice.getUsage();
    }

    @Override
    public short getUsagePage() {
        return (short) myDevice.getUsagePage();
    }
}
