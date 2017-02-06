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
package net.ash.HIDToVPADNetworkClient.manager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;

import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.XInputDevice14;
import com.ivan.xinput.exceptions.XInputNotLoadedException;

import lombok.Synchronized;
import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.controller.Controller.ControllerType;
import net.ash.HIDToVPADNetworkClient.controller.Hid4JavaController;
import net.ash.HIDToVPADNetworkClient.controller.LinuxDevInputController;
import net.ash.HIDToVPADNetworkClient.controller.XInput13Controller;
import net.ash.HIDToVPADNetworkClient.controller.XInput14Controller;
import net.ash.HIDToVPADNetworkClient.controller.XInputController;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;

public class ControllerManager{
	private static Map<String,Controller> attachedControllers = new HashMap<>();
    
	/**
	 * Detects all attached controller.
	 */
	@Synchronized("attachedControllers")
    public static void detectControllers() {
        String os = System.getProperty("os.name");
        //System.out.println("[ControllerDetector] OS: " + os);
        
        Map<String,ControllerType> connectedDevices = new HashMap<>();
        
        if (os.contains("Linux")) {
            connectedDevices.putAll(detectLinuxControllers());
        } else if (os.contains("Windows")) {
            connectedDevices.putAll(detectWindowsControllers());
        }
        
        /*TODO: Enable HID4Java again. Currently it's disabled because
        * We can either use the WiiU directly OR have XInput anyway.
        * Adding an option menu for enabling it?
        */
        //connectedDevices.putAll(detectHIDDevices());
        
        //Remove detached devices
        List<String> toRemove = new ArrayList<>();
        for(String s : attachedControllers.keySet()){
            if(!connectedDevices.containsKey(s)){
                toRemove.add(s);
            }
        }
        for(String remove : toRemove){
            attachedControllers.get(remove).destroyAll();
            attachedControllers.remove(remove);
        }
        
        //Add attached devices!
        for(Entry<String, ControllerType> entry : connectedDevices.entrySet()){
            String deviceIdentifier = entry.getKey();
            if(!attachedControllers.containsKey(deviceIdentifier)){
                Controller c = null;
                switch(entry.getValue()){
                    case HID4JAVA:
                        try {
                            c= new Hid4JavaController(deviceIdentifier);                            
                        } catch (ControllerInitializationFailedException e) {
                            //e.printStackTrace();
                        }
                        break;
                    case LINUX:
                        try {
                            c = new LinuxDevInputController(deviceIdentifier);
                        } catch (ControllerInitializationFailedException e) {
                            //e.printStackTrace();
                        }
                        break;
                    /*
                     * TODO:
                     * Currently the XInput will be set active automatically.
                     * But this should move to something for the settings? 
                     */
                    case XINPUT14:
                        try {
                            c = new XInput14Controller(deviceIdentifier);
                            c.setActive(true);
                        } catch (ControllerInitializationFailedException e) {
                            //e.printStackTrace();
                        }
                        break;
                    case XINPUT13:
                        try {
                            c = new XInput13Controller(deviceIdentifier);
                            c.setActive(true);
                        } catch (ControllerInitializationFailedException e) {
                            //e.printStackTrace();
                        }
                        break;
                default:
                    break;
                }        
                if(c != null){ //I don't like that starting the Thread happens here =/
                    new Thread(c).start();
                    attachedControllers.put(deviceIdentifier,c);
                }
            }
        }
    }
    
    @Synchronized("attachedControllers")
    public static List<Controller> getAttachedControllers() {
        return new ArrayList<>(attachedControllers.values());
    }
    
    private static Map<String, ControllerType> detectHIDDevices() {
        Map<String,ControllerType> connectedDevices = new HashMap<>();
        for (HidDevice device : HidManager.getHidServices().getAttachedHidDevices()) {
            if(device.getUsage() == 0x05 || device.getUsage() == 0x04){
                connectedDevices.put(device.getPath(),ControllerType.HID4JAVA);  
            }
        }
        return connectedDevices;
    }

    private static Map<String, ControllerType> detectWindowsControllers() {
        Map<String,ControllerType> result = new HashMap<>();
        ControllerType type = ControllerType.XINPUT13;
        if(XInputDevice.isAvailable() || XInputDevice14.isAvailable()) {
            if(XInputDevice14.isAvailable()){
                type = ControllerType.XINPUT14;
            }
            for(int i =0;i<4;i++){
                XInputDevice device;
                try {
                    device = XInputDevice.getDeviceFor(i);
                    if(device.poll() && device.isConnected()){ //Check if it is this controller is connected
                        result.put(XInputController.XINPUT_INDENTIFER + i, type);
                    }
                } catch (XInputNotLoadedException e) {
                    //This shouln't happen?
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private static Map<String, ControllerType> detectLinuxControllers() {
        Map<String,ControllerType> result = new HashMap<>();
        File devInput = new File("/dev/input");
        if (!devInput.exists()) return result;
        
        File[] linuxControllers = devInput.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("js"); //js0, js1, etc...
            }
        });
        
        for (File controller : linuxControllers) {
            result.put(controller.getAbsolutePath(),ControllerType.LINUX);
        }
        
        return result;
    }
    
    @Synchronized("attachedControllers")
    public static List<Controller> getActiveControllers() {
        List<Controller> active = new ArrayList<>();
        for(Controller c : attachedControllers.values()){
            if(c.isActive()){
                active.add(c);
            }
        }
        return active;
    }

    @Synchronized("attachedControllers")
    public static void deactivateAllAttachedControllers() {
        for(Controller c : attachedControllers.values()){
            c.setActive(false);
        }
    }

}
