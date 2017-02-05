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

import java.util.Arrays;

import org.hid4java.HidDevice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.HID4JavaManager;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

public class Hid4JavaController extends Controller {
    public Hid4JavaController(String identifier) throws ControllerInitializationFailedException {
        super(ControllerType.HID4JAVA, identifier);
    }

    private static final int PACKET_LENGTH = 128;
	
	@Getter @Setter(AccessLevel.PRIVATE) 
	private HidDevice hidDevice;

    @Override
	public boolean initController(String identifier) {
        HidDevice device = HID4JavaManager.getDeviceByPath(identifier);
        //device.setNonBlocking(true); //TODO: What does this do? This is done no in a extra Thread so it shouldn't matter.
        device.open();
        Utilities.sleep(20); //What a bit until is opened.
		if (device == null | !device.isOpen()) return false;
		setHidDevice(device);
		//System.out.println("ctrl: " + device.isOpen() + " " + device.getLastErrorMessage());
		return true;
	}
	
	@Override
    public byte[] pollLatestData() {
	    byte[] data = new byte[PACKET_LENGTH];
        int length = getHidDevice().read(data);
        if(length <= 0) return null;
        //if(length > data.length) System.out.println("WTF?");
        return Arrays.copyOf(data, length);
    }

	@Override
	public void destroyDriver() {
		getHidDevice().close();
	}
	
	@Override
	public short getVID() {
		return getHidDevice().getVendorId();
	}
	
	@Override
	public short getPID() {
		return getHidDevice().getProductId();
	}
}