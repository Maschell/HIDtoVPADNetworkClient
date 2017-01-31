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
package net.ash.HIDToVPADNetworkClient.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;

public class ControllerDetector extends Thread {
	private static final int POLL_TIMEOUT = 1000;
	private static final int SHORT_POLL_TIMEOUT = 100;
	
	private int lastHashCode = 0;
	@Override
	public void run() {
		for (;;) {
			if (GuiMain.instance() != null) {
				List<GuiController> tmp = detectControllers();
				if (lastHashCode != tmp.hashCode()) {
					lastHashCode = tmp.hashCode();
					GuiMain.instance().updateControllerList(tmp);
				}
				try {
					Thread.sleep(POLL_TIMEOUT);
				} catch (InterruptedException e) {}
			} else {
				try {
					Thread.sleep(SHORT_POLL_TIMEOUT);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	private static List<GuiController> detectControllers() {
		List<GuiController> controllers = new ArrayList<GuiController>();
		
		String os = System.getProperty("os.name");
		//System.out.println("[ControllerDetector] OS: " + os);
		
		if (os.contains("Linux")) {
			detectLinuxControllers(controllers);
		} else if (os.contains("Windows")) {
			System.out.println("Running on Windows! XInput coming soon."); //XXX debug text (win32)
		}
		
		for (HidDevice device : HidManager.getHidServices().getAttachedHidDevices()) {
			controllers.add(new GuiController(GuiControllerType.HID4JAVA, device.getPath()));
		}
		return controllers;
	}
	
	private static void detectLinuxControllers(List<GuiController> controllers) {
		File devInput = new File("/dev/input");
		if (!devInput.exists()) return;
		
		File[] linuxControllers = devInput.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("js"); //js0, js1, etc...
			}
		});
		
		for (File controller : linuxControllers) {
			controllers.add(new GuiController(GuiControllerType.LINUX, controller.getAbsolutePath()));
		}
	}
}
