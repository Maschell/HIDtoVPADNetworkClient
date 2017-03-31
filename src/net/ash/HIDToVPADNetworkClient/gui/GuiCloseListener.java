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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.ash.HIDToVPADNetworkClient.Main;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public class GuiCloseListener implements WindowListener {

    @Override
    public void windowClosing(WindowEvent arg0) {
        Settings.saveSettings();
        Main.initiateShutdown();
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
        // not used
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        // not used
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
        // not used
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
        // not used
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
        // not used
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
        // not used
    }
}
