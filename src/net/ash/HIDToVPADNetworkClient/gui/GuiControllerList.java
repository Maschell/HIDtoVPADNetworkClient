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
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class GuiControllerList extends JPanel {
	private static final long serialVersionUID = 1L;

	private JPanel innerScrollPanel;
	private ActionListener currentActionListener = null;
	
	public GuiControllerList() {
		super(new BorderLayout());
		
		innerScrollPanel = new JPanel();
		innerScrollPanel.setLayout(new BoxLayout(innerScrollPanel, BoxLayout.PAGE_AXIS));
		add(new JScrollPane(innerScrollPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	}
	
	public synchronized List<GuiController> getControllers() {
		List<GuiController> controllers = new ArrayList<GuiController>();
		
		for (Component component : innerScrollPanel.getComponents()) {
			if (component instanceof GuiControllerListItem) {
				controllers.add(((GuiControllerListItem)component).getData());
			}
		}
		
		return controllers;
	}
	
	public synchronized void updateControllerList(List<GuiController> controllers) {
		System.out.println("[GuiControllerList] Updating controller list..."); //XXX debug text
		
		HashMap<Integer, GuiController> components = new HashMap<Integer, GuiController>();
		for (Component component : innerScrollPanel.getComponents()) {
			if (component instanceof GuiControllerListItem) {
				components.put(component.hashCode(), ((GuiControllerListItem)component).getData());
			}
		}
		
		List<GuiControllerListItem> newComponents = new ArrayList<GuiControllerListItem>();
		for (GuiController controller : controllers) {
			GuiControllerListItem i;
			if (components.containsKey(controller.hashCode())) {
				i = new GuiControllerListItem(components.get(controller.hashCode()));
			} else {
				i = new GuiControllerListItem(controller);
			}
			newComponents.add(i);
		}
		
		innerScrollPanel.removeAll();
		for (GuiControllerListItem component : newComponents) {
			component.addActionListener(currentActionListener);
			innerScrollPanel.add(component);
		}
		
		//TODO research performance impact - 300+ms on swing.RepaintManager?
		innerScrollPanel.revalidate();
		revalidate();
		innerScrollPanel.repaint();
		repaint();
	}
	
	public synchronized void setActionListener(ActionListener l) {
		currentActionListener = l;
		for (Component c : innerScrollPanel.getComponents()) {
			try {
				((AbstractButton)c).addActionListener(l);
			} catch (ClassCastException e) {
				System.out.println("[GuiControllerList] Bad cast on " + c.getClass().getName() + " to AbstractButton!");
			}
		}
	}
}
