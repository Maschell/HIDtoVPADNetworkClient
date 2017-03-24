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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.Timer;

import lombok.Getter;
import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

public class GuiControllerListItem extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    @Getter private final Controller controller;
    private JCheckBox checkbox;

    public GuiControllerListItem(Controller data) {
        super(new BorderLayout());

        setMinimumSize(new Dimension(300, 30));
        setPreferredSize(new Dimension(300, 30));
        setMaximumSize(new Dimension(2000, 30));

        this.controller = data;

        checkbox = new JCheckBox(getFlavorText());
        checkbox.setSelected(data.isActive());
        checkbox.addActionListener(this);
        add(checkbox);

        int delay = 100; // milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkbox.setEnabled(NetworkManager.getInstance().isConnected());
                checkbox.setSelected(controller.isActive());
            }
        };
        new Timer(delay, taskPerformer).start();
    }

    private String getFlavorText() {
        return controller.getInfoText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean selected = ((JCheckBox) e.getSource()).isSelected();
        controller.setActive(selected);
        checkbox.setSelected(controller.isActive());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controller == null) ? 0 : controller.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GuiControllerListItem other = (GuiControllerListItem) obj;
        if (controller == null) {
            if (other.controller != null) return false;
        } else if (!controller.equals(other.controller)) return false;
        return true;
    }
}
