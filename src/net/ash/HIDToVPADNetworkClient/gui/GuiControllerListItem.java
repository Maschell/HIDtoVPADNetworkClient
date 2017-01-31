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
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class GuiControllerListItem extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private GuiController data = null;
	private JCheckBox checkbox;

	public GuiControllerListItem(GuiController data) {
		super(new BorderLayout());
		
		setMinimumSize(new Dimension (300, 30));
		setPreferredSize(new Dimension(300, 30));
		setMaximumSize(new Dimension(2000, 30));
		
		this.data = data;
		
		checkbox = new JCheckBox(getFlavorText());
		checkbox.setSelected(data.getActiveState());
		add(checkbox);
	}
	
	private String getFlavorText() {
		switch (data.getType()) {
		case HID4JAVA:
			return "USB HID on " + data.getId().toString();
		case LINUX:
			return "Linux controller on " + data.getId().toString();
		default:
			return data.toString();
		}
	}
	
	public void addActionListener(ActionListener l) {
		checkbox.addActionListener(l);
	}
	
	public GuiController getData() {
		return data;
	}
	
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
