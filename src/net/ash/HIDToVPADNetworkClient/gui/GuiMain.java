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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxListener;

@Log
public class GuiMain extends JPanel implements MessageBoxListener {
    private static final long serialVersionUID = 1L;
    private static GuiMain instance;

    private static GuiMain createGUI() {
        JFrame frame = new JFrame("HID To VPAD Network Client");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new GuiCloseListener());

        GuiMain instance = new GuiMain();

        JComponent newContentPane = instance;
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
        return instance;
    }

    public GuiMain() {
        super(new BorderLayout());

        GuiControllerList leftControllerList = new GuiControllerList();
        leftControllerList.setPreferredSize(new Dimension(400, 200));
        add(leftControllerList, BorderLayout.CENTER);
        GuiInputControls rightSideControls = GuiInputControls.getInstance();
        add(rightSideControls, BorderLayout.LINE_END);
    }

    public synchronized static GuiMain getInstance() {
        if (instance == null) {
            instance = createGUI();
        }
        return instance;
    }

    @Override
    public void showMessageBox(MessageBox msg) {
        if (msg == null || msg.getMessage() == null) {
            log.info("Can't show the message box");
        }
        String real_title = "HID To VPAD Network Client";

        JOptionPane.showMessageDialog(this, msg.getMessage(), real_title, msg.getType());
    }
}
