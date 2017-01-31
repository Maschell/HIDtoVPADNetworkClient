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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import net.ash.HIDToVPADNetworkClient.util.HandleFoundry;

public class LinuxDevInputController extends Thread implements Controller {
	public static final int NUM_SUPPORTED_AXIS = 10; //possibly off-by-one
	public static final int CONTROLLER_DATA_SIZE = Long.BYTES + (Byte.BYTES * NUM_SUPPORTED_AXIS);
	
	private static final byte JS_EVENT_BUTTON = 0x01;
	private static final byte JS_EVENT_INIT = (byte)0x80;
	private static final byte JS_EVENT_AXIS = 0x02;
	
	private DataInputStream controller;
	private ControllerData controllerData;
	
	private String id;
	
	private short deviceSlot = 0;
	private byte padSlot = 0;
	
	private boolean shouldProcessEvents = false;
	private boolean shutdown = false;
	
	public LinuxDevInputController() {
		super("LinuxDevInputController");
		controllerData = new ControllerData((short)0, (short)0, 0, new byte[CONTROLLER_DATA_SIZE]);
	}
	
	@Override
	public void run() {
		for (;;) {
			for (;;) {
				if (!shouldProcessEvents) break;
				
				processNextControllerEvent();
			}
			
			if (shutdown) break;
			/* Not polling. Wait for setPollingState to wake us up. */
			try {
				this.wait();
			} catch (InterruptedException e) {}
		} //for (;;)
	}
	
	private long buttonState = 0;
	private byte[] axisState = new byte[NUM_SUPPORTED_AXIS];
	private void processNextControllerEvent() {
		//Read out next event from controller
		/*int time;*/
		short value;
		byte type, number;
		try {
			/*time = */controller.readInt();
			value = controller.readShort();
			type = controller.readByte();
			number = controller.readByte();
		} catch (IOException e) {
			System.err.println("[LinuxDevInputController] Couldn't read from controller!");
			e.printStackTrace();
			System.out.println("[LinuxDevInputController] Detaching...");
			ControllerManager.instance().detachController(this);
			return;
		}
		
		//Treat init events as normal (clear init bit)
		type &= ~JS_EVENT_INIT;
		
		if (type == JS_EVENT_BUTTON) {
			if (number >= Long.SIZE) {
				System.out.println("[LinuxDevInputController] Button number " + number + " out of range; ignoring");
				return;
			}
			
			if (value != 0) {
				//Set bit with button number
				buttonState |= (1 << number);
			} else {
				//Clear bit with button number
				buttonState &= ~(1 << number);
			}
		} else if (type == JS_EVENT_AXIS) {
			if (number >= NUM_SUPPORTED_AXIS) {
				System.out.println("[LinuxDevInputController] Axis number " + number + " out of range; ignoring");
				return;
			}
			//Do byteswap
			value = (short)(((value & 0xFF) << Byte.SIZE) | ((value & 0xFF00) >> Byte.SIZE));
			//Convert to unsigned byte and store
			axisState[number] = (byte)(((value + Short.MAX_VALUE + 1) >> 8) & 0xFF);
		}
		
		byte[] newData = new byte[CONTROLLER_DATA_SIZE];
		//Copy in button states
		for (int i = 0; i < Long.BYTES; i++) {
			newData[i] = (byte)((buttonState >> (i * Byte.SIZE)) & 0xFF);
		}
		//Copy in axis data
		for (int i = Long.BYTES; i < CONTROLLER_DATA_SIZE; i++) {
			newData[i] = axisState[i - Long.BYTES];
		}
		synchronized (controllerData) {
			controllerData.data = newData;
		}
	}
	
	@Override
	public ControllerData getLatestData() {		
		synchronized (controllerData) {
			return new ControllerData(getVID(), getPID(), getHandle(), controllerData.getData());
		}
	}
	
	@Override
	public boolean initController(Object arg) {
		try {
			controller = new DataInputStream(new BufferedInputStream(new FileInputStream(arg.toString())));
		} catch (Exception e) {
			System.err.println("[LinuxDevInputController] Couldn't open " + arg.toString() + " as file!");
			e.printStackTrace();
			return false;
		}
		
		fakevid = (short)(arg.hashCode() & 0xFFFF);
		fakepid = (short)((arg.hashCode() >> Short.BYTES) & 0xFFFF);
		System.out.println("[LinuxDevInputController] " + arg.toString() + " fakevid: " + Integer.toHexString((int)fakevid & 0xFFFF) + " fakepid: " + Integer.toHexString((int)fakepid & 0xFFFF));
		
		id = arg.toString();
		
		return true;
	}
	
	@Override
	public void setPollingState(boolean poll) {
		shouldProcessEvents = poll;
		if (this.getState() == Thread.State.NEW) {
			this.start();
		} else if (this.getState() == Thread.State.WAITING){
			this.notify();
		}
	}

	@Override
	public void destroy() {
		shutdown = true;
		setPollingState(false);
		
		if (!this.equals(Thread.currentThread())) {
			while (this.getState() != Thread.State.TERMINATED) {}
		}
		
		try {
			controller.close();
		} catch (IOException e) {}
	}
	
	private short fakevid;
	private short getVID() {
		return fakevid;
	}
	
	private short fakepid;
	private short getPID() {
		return fakepid;
	}

	@Override
	public String getID() {
		return id;
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

	@Override
	public String toString() {
		return "[" + super.toString() + ";VID," + Integer.toHexString((int)getVID() & 0xFFFF) + ";PID," + Integer.toHexString((int)getPID() & 0xFFFF) + ";run," + shouldProcessEvents + ((controller == null) ? ";uninitialised]" : ";initialised]");
	}
}
