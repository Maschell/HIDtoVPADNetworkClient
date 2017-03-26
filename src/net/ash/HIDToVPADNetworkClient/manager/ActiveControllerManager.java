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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.network.NetworkHIDDevice;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

@Log
public class ActiveControllerManager implements Runnable {
    private static ActiveControllerManager instance = new ActiveControllerManager();

    private ActiveControllerManager() {
    }

    public static ActiveControllerManager getInstance() {
        return instance;
    }

    @Override
    public void run() { // TODO: Add mechanism to stop these threads?
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateControllerStates();
                    ControllerManager.detectControllers();
                    Utilities.sleep(Settings.DETECT_CONTROLLER_INTERVAL);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    handleControllerInputs();
                    Utilities.sleep(Settings.HANDLE_INPUTS_INTERVAL);
                }
            }
        }).start();
    }

    private Map<Controller, NetworkHIDDevice> activeControllers = new HashMap<Controller, NetworkHIDDevice>();

    public void updateControllerStates() {
        List<Controller> currentControllers = ControllerManager.getActiveControllers();

        List<Controller> toAdd = new ArrayList<Controller>();
        List<Controller> toRemove = new ArrayList<Controller>();
        synchronized (activeControllers) {
            // Adding all missing.

            for (Controller c : currentControllers) {
                if (!activeControllers.containsKey(c)) {
                    log.info("Added " + c);
                    toAdd.add(c);
                }
            }

            // removing all old
            for (Controller c : activeControllers.keySet()) {
                if (!currentControllers.contains(c)) {
                    log.info("Removed " + c);
                    toRemove.add(c);
                }
            }
        }

        addController(toAdd);
        removeController(toRemove);
    }

    private void removeController(List<Controller> toRemove) {
        synchronized (activeControllers) {
            for (Controller c : toRemove) {
                NetworkManager.getInstance().removeHIDDevice(activeControllers.get(c));
                // c.destroyDriver(); Removing it from the list doesn't require to close the connection.
                activeControllers.remove(c);
            }
        }
    }

    private void addController(List<Controller> toAdd) {
        synchronized (activeControllers) {
            for (Controller c : toAdd) {
                NetworkHIDDevice hiddevice = new NetworkHIDDevice(c.getVID(), c.getPID());
                hiddevice.sendAttach();
                NetworkManager.getInstance().addHIDDevice(hiddevice);
                activeControllers.put(c, hiddevice);
            }
        }
    }

    private void handleControllerInputs() {
        synchronized (activeControllers) {
            for (Entry<Controller, NetworkHIDDevice> entry : activeControllers.entrySet()) {
                byte[] data = entry.getKey().getLatestData();
                if (data != null) {
                    NetworkHIDDevice device = entry.getValue();
                    device.sendRead(data);
                }
            }
        }
    }

    public void attachAllActiveControllers() {
        synchronized (activeControllers) {
            for (Entry<Controller, NetworkHIDDevice> entry : activeControllers.entrySet()) {
                NetworkHIDDevice device = entry.getValue();
                device.sendAttach();
            }
        }
    }

    /**
     * 
     * @param HIDhandle
     * @return returns the controller for the given handle. returns null if the controller with the given handle is not found.
     */
    public Controller getControllerByHIDHandle(int HIDhandle) {
        for (Entry<Controller, NetworkHIDDevice> entry : activeControllers.entrySet()) {
            if (entry.getValue().getHidHandle() == HIDhandle) {
                return entry.getKey();
            }
        }
        return null;
    }
}
