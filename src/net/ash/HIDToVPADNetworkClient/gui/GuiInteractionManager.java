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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import net.ash.HIDToVPADNetworkClient.controller.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

public class GuiInteractionManager implements ActionListener {
	private static GuiInteractionManager instance = null;

	public GuiInteractionManager() throws Exception {
		if (instance != null) {
			throw new Exception("GuiInputControls already has an instance!");
		}
		instance = this;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		 * Swing GUI events
		 */
		if (e.getSource() instanceof Component) {
			Component source = (Component)e.getSource();
			Container parent = source.getParent();
			
			/*
			 * Action handler code for GuiControllerListItem
			 */
			if (parent instanceof GuiControllerListItem) {
				GuiControllerListItem rparent = (GuiControllerListItem)parent;
				JCheckBox rsource = (JCheckBox)source;
				
				rparent.getData().setActiveState(rsource.isSelected());
				
				if (NetworkManager.instance().isRunning()) {
					ControllerManager.instance().updateControllers(GuiMain.instance().getControllers());
				}
			/*
			 * Action handler for GuiInputControls
			 */
			} else if (parent instanceof GuiInputControls) {
				GuiInputControls rparent = (GuiInputControls)parent;
				/*
				 * (Dis)connect button
				 */
				if (source instanceof JButton) {
					if (NetworkManager.instance().isRunning()) {
						disconnect();
					} else {
						NetworkManager.instance().setPacketInterval(Integer.parseInt(rparent.getPacketIntervalTextBox().getText()));
						if (ControllerManager.instance().startConnection(GuiMain.instance().getIPText(), GuiMain.instance().getControllers())) {
							rparent.getIpTextBox().setEnabled(false);
							rparent.getPacketIntervalTextBox().setEnabled(false);
							rparent.getConnectButton().setText("Disconnect");
							GuiInputControls.instance().getStatusLabel().setText("Connected!");
						} else {
							ControllerManager.instance().stopConnection();
							GuiInputControls.instance().getStatusLabel().setText("Connection Failed!");
						}
					}
				}
			}
		}
	}
	
	public void disconnect() {
		ControllerManager.instance().stopConnection();
		GuiInputControls.instance().getIpTextBox().setEnabled(true);
		GuiInputControls.instance().getPacketIntervalTextBox().setEnabled(true);
		GuiInputControls.instance().getConnectButton().setText("Connect");
		GuiInputControls.instance().getStatusLabel().setText("Disconnected.");
	}
	
	public static GuiInteractionManager instance() {
		return instance;
	}
}
