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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.ash.HIDToVPADNetworkClient.gui.GuiController;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

public class ControllerManager {
	private static ControllerManager instance;
	
	public ControllerManager() throws Exception {
		if (instance != null) {
			throw new Exception("ControllerManager already has an instance!");
		}
		instance = this;
	}
	
	public boolean startConnection(String ip, List<GuiController> controllersIn) {
		List<Controller> controllers = setupControllers(controllersIn);
		
		//Boot up all drivers
		for (Controller c : controllers) {
			c.setPollingState(true);
			
			NetworkManager.instance().addController(c);
		}
		
		NetworkManager.instance().connect(ip);
		return NetworkManager.instance().isConnected();
	}
	
	public void stopConnection() {
		NetworkManager.instance().removeAllControllers();
		NetworkManager.instance().disconnect();
	}
	
	public void updateControllers(List<GuiController> controllersIn) {
		HashSet<Integer> m = new HashSet<Integer>();
		for (GuiController g : controllersIn) {
			if (!g.getActiveState()) continue;
			m.add(g.getId().hashCode());
		}
		
		HashMap<Integer, Controller> d = NetworkManager.instance().getControllers();
		for (Controller c : d.values()) {
			if (!m.contains(c.getID().hashCode())) {
				NetworkManager.instance().removeController(c);
			}
		}
		
		List<GuiController> list = new ArrayList<GuiController>();
		for (GuiController g : controllersIn) {
			if (!d.containsKey(g.getId().hashCode())) {
				list.add(g);
			}
		}
		
		for (Controller c : setupControllers(list)) {
			c.setPollingState(true);
			NetworkManager.instance().addController(c);
		}
	}
	
	private List<Controller> setupControllers(List<GuiController> controllers) {
		List<Controller> out = new ArrayList<Controller>();
		
		for (GuiController g : controllers) {
			if (!g.getActiveState()) continue;
			
			Controller ctrl;
			
			switch (g.getType()) {
			case HID4JAVA:
				ctrl = new Hid4JavaController();
				break;
			case LINUX:
				ctrl = new LinuxDevInputController();
				break;
			default:
				System.out.println("[ControllerManager] Unsupported controller type " + g.getType().name());
				continue;
			}
			
			ctrl.initController(g.getId());
			
			out.add(ctrl);
		}
		
		return out;
	}
	
	public void detachController(Controller c) {
		HashMap<Integer, Controller> m = NetworkManager.instance().getControllers();
		if (m.containsKey(c.getID().hashCode())) {
			NetworkManager.instance().removeController(c);
		}
	}
	
	public static ControllerManager instance() {
		return instance;
	}
}
