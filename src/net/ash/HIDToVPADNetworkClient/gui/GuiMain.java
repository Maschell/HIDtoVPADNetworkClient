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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.ash.HIDToVPADNetworkClient.Main;

public class GuiMain extends JPanel {
	private static final long serialVersionUID = 1L;
	private static GuiMain instance;

	public static void createGUI() {
		JFrame frame = new JFrame("HID To VPAD Network Client");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new GuiCloseListener());
		
		instance = new GuiMain();
		JComponent newContentPane = instance;
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);
		
		frame.pack();
		frame.setVisible(true);
	}

	private GuiControllerList leftControllerList;
	private GuiInputControls rightSideControls;
	public GuiMain() {
		super(new BorderLayout());
		
		try {
			new GuiInteractionManager();
		} catch (Exception e) {
			e.printStackTrace();
			Main.fatal();
		}
		
		
		leftControllerList = new GuiControllerList();
		leftControllerList.setPreferredSize(new Dimension(300, 100));
		add(leftControllerList, BorderLayout.CENTER);
		leftControllerList.setActionListener(GuiInteractionManager.instance());
		
		try {
			rightSideControls = new GuiInputControls();
		} catch (Exception e) {
			e.printStackTrace();
			Main.fatal();
		}
		add(rightSideControls, BorderLayout.LINE_END);
	}
	
	public void updateControllerList(List<GuiController> controllers) {
		leftControllerList.updateControllerList(controllers);
	}
	
	public List<GuiController> getControllers() {
		return leftControllerList.getControllers();
	}
	
	public String getIPText() {
		return rightSideControls.getIpTextBox().getText();
	}
	
	public static GuiMain instance() {
		return instance;
	}
}
