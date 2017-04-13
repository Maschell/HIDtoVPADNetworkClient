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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import lombok.Getter;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

public class LinuxDevInputController extends Controller implements Runnable {
    public static final int NUM_SUPPORTED_AXIS = 10; // possibly off-by-one
    public static final int CONTROLLER_DATA_SIZE = (Long.SIZE / Byte.SIZE) + ((Byte.SIZE / Byte.SIZE) * NUM_SUPPORTED_AXIS);

    private static final byte JS_EVENT_BUTTON = 0x01;
    private static final byte JS_EVENT_INIT = (byte) 0x80;
    private static final byte JS_EVENT_AXIS = 0x02;

    private DataInputStream controller;
    
    @Getter private short VID;
    @Getter private short PID;
    
    private String name;

    private long buttonState = 0;
    private byte[] axisState = new byte[NUM_SUPPORTED_AXIS];

    public LinuxDevInputController(String identifier) throws ControllerInitializationFailedException {
        super(ControllerType.LINUX, identifier);
    }

    @Override
    public boolean initController(String identifier) {
        try {
            controller = new DataInputStream(new BufferedInputStream(new FileInputStream(identifier)));
        } catch (Exception e) {
            System.err.println("[LinuxDevInputController] Couldn't open " + identifier + " as file!");
            e.printStackTrace();
            return false;
        }

        try {
            doSysFs(identifier);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("oops");
        }
        
        if (VID == 0 || PID == 0) {
            VID = ((short) (identifier.hashCode() & 0xFFFF));
            PID = ((short) ((identifier.hashCode() >> (Short.SIZE / Byte.SIZE)) & 0xFFFF));
            System.out.println("[LinuxDevInputController] " + identifier.toString() + " fakevid: " + Integer.toHexString((int) getVID() & 0xFFFF) + " fakepid: "
                    + Integer.toHexString((int) getPID() & 0xFFFF));
        }

        return true;
    }

    //This could probably do with some cleanup
    public void doSysFs(String identifier) throws Exception {
        Process querySysFs = Runtime.getRuntime().exec("udevadm info -q path " + identifier);
        querySysFs.waitFor();
        
        String sysfs_path = "/sys" + Utilities.getStringFromInputStream(querySysFs.getInputStream()).trim() + "/device";
        querySysFs.destroy();
        File sysfs = new File(sysfs_path);
        if (!sysfs.exists()) return;
        
        char[] nameBuf = new char[1024];
        FileReader nameGet = new FileReader(sysfs_path + "/name");
        nameGet.read(nameBuf);
        nameGet.close();
        name = new String(nameBuf).trim();
        
        char[] vidBuf = new char[6];
        FileReader vidGet = new FileReader(sysfs_path + "/id/vendor");
        vidGet.read(vidBuf);
        vidGet.close();
        short vid = Short.parseShort(new String(vidBuf).trim(), 16);
        this.VID = vid;
        
        char[] pidBuf = new char[6];
        FileReader pidGet = new FileReader(sysfs_path + "/id/product");
        pidGet.read(pidBuf);
        pidGet.close();
        short pid = Short.parseShort(new String(pidBuf).trim(), 16);
        this.PID = pid;
    }
    
    @Override
    public byte[] pollLatestData() {
        DataInputStream inputStream = this.controller;
        // Read out next event from controller
        /* int time; */
        short value;
        byte type, number;
        try {
            /* time = */inputStream.readInt();
            value = inputStream.readShort();
            type = inputStream.readByte();
            number = inputStream.readByte();
        } catch (IOException e) {
            if (!isActive()) return null; //"Stream closed" when removing
            System.err.println("[LinuxDevInputController] Couldn't read from controller!");
            e.printStackTrace();
            System.out.println("[LinuxDevInputController] Detaching...");
            setActive(false);
            return null;
        }

        // Treat init events as normal (clear init bit)
        type &= ~JS_EVENT_INIT;

        if (type == JS_EVENT_BUTTON) {
            if (number >= Long.SIZE) {
                System.out.println("[LinuxDevInputController] Button number " + number + " out of range; ignoring");
                return null;
            }

            if (value == 0) {
                // Clear bit with button number
                buttonState &= ~(1 << number);
            } else {
                // Set bit with button number
                buttonState |= (1 << number);
            }
        } else if (type == JS_EVENT_AXIS) {
            if (number >= NUM_SUPPORTED_AXIS) {
                System.out.println("[LinuxDevInputController] Axis number " + number + " out of range; ignoring");
                return null;
            }
            // Do byteswap
            value = (short) (((value & 0xFF) << Byte.SIZE) | ((value & 0xFF00) >> Byte.SIZE));
            // Convert to unsigned byte and store
            axisState[number] = (byte) (((value + Short.MAX_VALUE + 1) >> 8) & 0xFF);
        }

        byte[] newData = new byte[CONTROLLER_DATA_SIZE];
        // Copy in button states
        for (int i = 0; i < (Long.SIZE / Byte.SIZE); i++) {
            newData[i] = (byte) ((buttonState >> (i * (Byte.SIZE / Byte.SIZE))) & 0xFF);
        }
        // Copy in axis data
        for (int i = (Long.SIZE / Byte.SIZE); i < CONTROLLER_DATA_SIZE; i++) {
            newData[i] = axisState[i - (Long.SIZE / Byte.SIZE)];
        }
        
        return newData;
    }

    @Override
    protected void doSleepAfterPollingData() {
        // This is event driven (aka pollLatestData() is blocking anyway until
        // we have data), we don't need to sleep it all.
    }

    @Override
    public void destroyDriver() {
        try {
            controller.close();
        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
        return "[" + super.toString() + ";VID," + Integer.toHexString((int) getVID() & 0xFFFF) + ";PID," + Integer.toHexString((int) getPID() & 0xFFFF)
                + ";name," + name + ";run," + isActive() + ((controller == null) ? ";uninitialised]" : ";initialised]");
    }

    @Override
    public String getInfoText() {
        return ((name != null) ? name : "Linux controller") + " on " + getIdentifier();    
    }
}
