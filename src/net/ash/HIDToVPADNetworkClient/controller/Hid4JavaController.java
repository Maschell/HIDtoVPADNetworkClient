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

import org.hid4java.HidDevice;
import org.hid4java.HidManager;

import net.ash.HIDToVPADNetworkClient.util.HandleFoundry;

public class Hid4JavaController implements Controller {
	static final int PACKET_LENGTH = 64;
	
	private String path = null;
	private HidDevice controller = null;
	
	@Override
	public boolean initController(Object arg) {
		for (HidDevice device : HidManager.getHidServices().getAttachedHidDevices()) {
			if (device.getPath().equals(arg.toString())) {
				controller = device;
				controller.setNonBlocking(true);
				
				path = arg.toString();
				
				break;
			}
		}
		
		System.out.println("ctrl: " + controller.open() + " " + controller.getLastErrorMessage());
		if (controller == null | !controller.isOpen()) return false;
		return true;
	}
	
	@Override
	public void setPollingState(boolean poll) {
		
	}

	@Override
	public ControllerData getLatestData() {
		ControllerData data = new ControllerData(getVID(), getPID(), getHandle(), new byte[1200]);
		System.out.println("Data size: " + controller.read(data.data, 500));
		System.out.println("hrm: " + controller.getLastErrorMessage());
		return data;
	}

	@Override
	public void destroy() {
		controller.close();
	}
	
	private short getVID() {
		return controller.getVendorId();
	}

	private short getPID() {
		return controller.getProductId();
	}

	private short deviceSlot = 0;
	private byte padSlot = 0;
	
	@Override
	public void setSlotData(short deviceSlot, byte padSlot) {
		this.deviceSlot = deviceSlot;
		this.padSlot = padSlot;
	}

	@Override
	public short getDeviceSlot() {
		return deviceSlot;
	}

	@Override
	public byte getPadSlot() {
		return padSlot;
	}

	private int handle = 0;
	@Override
	public int getHandle() {
		if (handle == 0) {
			handle = HandleFoundry.next();
		}
		return handle;
	}

	@Override
	public String getID() {
		return path;
	}
}
