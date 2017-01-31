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
package net.ash.HIDToVPADNetworkClient;

import javax.swing.SwingUtilities;

import org.hid4java.HidManager;

import net.ash.HIDToVPADNetworkClient.controller.ControllerManager;
import net.ash.HIDToVPADNetworkClient.gui.ControllerDetector;
import net.ash.HIDToVPADNetworkClient.gui.GuiMain;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

/* Ash's todo list
 * TODO finish Hid4JavaController
 * TODO locale
 */

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		
		try {
			new ControllerManager();
			new NetworkManager();
		} catch (Exception e) {
			e.printStackTrace();
			fatal();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GuiMain.createGUI();
			}
		});
		
		new ControllerDetector().start();
	}
	
	public static void fatal() {
		System.err.println("HID To VPAD Network Client encountered an irrecoverable error.");
		System.err.println("Exiting...");
		System.exit(1);
	}
	
	public static void initiateShutdown() {
		HidManager.getHidServices().shutdown();
		System.exit(0);
	}
}