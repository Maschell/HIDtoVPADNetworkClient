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
//This file is only here so I can remember just how shocking my first attempt
//at a driver like this was. It should never, NEVER be used.

/*	I'M SO SORRY
 *  Please do not use this
 */

package net.ash.HIDToVPADNetworkClient.controller;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.ash.HIDToVPADNetworkClient.util.BlockingIOStabbifier;
import net.ash.HIDToVPADNetworkClient.util.WakeupThread;

@Deprecated
public class LinuxDevInputControllerLegacyDriver extends Thread implements Controller  {
	private DataInputStream controller;
	private String path;
	
	@Override
	public boolean initController(Object arg) {
		try {
			path = (String)arg;
		} catch (ClassCastException e) {
			System.err.println("LinuxDevInputController recieved bad argument!");
			e.printStackTrace();
			return false;
		}
	
		try {
			controller = new DataInputStream(new BlockingIOStabbifier(path));
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + path + "!");
			e.printStackTrace();
			return false;
		}
		data = new ControllerData();
		data.data = new byte[100]; //100 BUTTON MAX
		return true;
	}

	private boolean shouldPoll = false;
	WakeupThread wakeup;
	//@Override
	public void setPollingState(boolean poll, int pollInterval) {
		if (poll) {
			if (this.getState() == Thread.State.NEW) {
				shouldPoll = poll;
				this.start();
			}
			wakeup = new WakeupThread(LinuxDevInputControllerThreadLock);
			wakeup.setTimeout(pollInterval);
			wakeup.start();
		}
		shouldPoll = poll;
	}
	
	Object LinuxDevInputControllerThreadLock = new Object();
	@Override
	public void run() {
		for (;;) {
			while (shouldPoll) {
				updateControllerData();
			
				synchronized (LinuxDevInputControllerThreadLock) {
					try {
						LinuxDevInputControllerThreadLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			wakeup.tryStop();
			synchronized (LinuxDevInputControllerThreadLock) {
				try {
					LinuxDevInputControllerThreadLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private ControllerData data;
	
	private void updateControllerData() {
		try {
			while (controller.available() > 8) { /* 8 bytes to a joystick packet */
				/*int time = */controller.readInt();
				short val = controller.readShort();
				byte type = controller.readByte();
				byte num = controller.readByte();
				num *= 2;
				if (type == 0x1 && !(num >= 100)) {
					synchronized (data) {
						data.data[num] = (byte)(val & 0xFF);
						data.data[num + 1] = (byte)((val >> 8) & 0xFF);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ControllerData getLatestData() {
		synchronized (data) {
			return data;
		}
	}
	
	public short getPID() {
		return data.getPID();
	}
	
	public short getVID() {
		return data.getVID();
	}
	
	public void setFakePid(short pid) {
		//this.data.pid = pid;
	}
	
	public void setFakeVid(short vid) {
		//this.data.vid = vid;
	}

	@Override
	public void setSlotData(short deviceSlot, byte padSlot) {
		//  Auto-generated method stub
		
	}

	@Override
	public short getDeviceSlot() {
		//  Auto-generated method stub
		return 0;
	}

	@Override
	public byte getPadSlot() {
		//  Auto-generated method stub
		return 0;
	}

	@Override
	public int getHandle() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	public String getID() {
		// Auto-generated method stub
		return null;
	}

	@Override
	public void setPollingState(boolean poll) {
		//  Auto-generated method stub
		
	}
}
