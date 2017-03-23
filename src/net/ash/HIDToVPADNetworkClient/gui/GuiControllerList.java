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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;

public class GuiControllerList extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPanel innerScrollPanel;

    public GuiControllerList() {
        super(new BorderLayout());

        innerScrollPanel = new JPanel();
        innerScrollPanel.setLayout(new BoxLayout(innerScrollPanel, BoxLayout.PAGE_AXIS));
        add(new JScrollPane(innerScrollPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        int delay = 1000; // milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateControllerList();
            }
        };
        new Timer(delay, taskPerformer).start();
    }

    public synchronized void updateControllerList() {
        // System.out.println("[GuiControllerList] Updating controller
        // list..."); //XXX debug text

        boolean repaintNeeded = false;

        List<Controller> attachedControllers = ControllerManager.getAttachedControllers();

        List<GuiControllerListItem> newComponents = new ArrayList<GuiControllerListItem>();

        Map<Controller, GuiControllerListItem> components = new HashMap<Controller, GuiControllerListItem>();
        for (Component component : innerScrollPanel.getComponents()) {
            if (component instanceof GuiControllerListItem) {
                GuiControllerListItem comp = (GuiControllerListItem) component;
                Controller cont = comp.getController();
                if (attachedControllers.contains(cont)) {
                    components.put(cont, comp);
                } else {// Controller removed
                    repaintNeeded = true;
                }
            }
        }

        // Build new list of components.
        for (Controller controller : attachedControllers) {
            GuiControllerListItem i = null;
            if (components.containsKey(controller)) {
                newComponents.add(components.get(controller));
            } else { // New controller was added
                repaintNeeded = true;
                i = new GuiControllerListItem(controller);
                newComponents.add(i);
            }
        }

        if (repaintNeeded) {
            innerScrollPanel.removeAll();
            for (GuiControllerListItem component : newComponents) {
                innerScrollPanel.add(component);
            }

            innerScrollPanel.revalidate();
            revalidate();
            innerScrollPanel.repaint();
            repaint();
        }
    }
}
