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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.ash.HIDToVPADNetworkClient.Main;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;

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

        leftControllerList = new GuiControllerList();
        leftControllerList.setPreferredSize(new Dimension(300, 100));
        add(leftControllerList, BorderLayout.CENTER);

        try {
            rightSideControls = new GuiInputControls();
        } catch (Exception e) {
            e.printStackTrace();
            Main.fatal();
        }
        add(rightSideControls, BorderLayout.LINE_END);
        
        int delay = 100;
        ActionListener messageBoxPerformer = new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		MessageBox msg = MessageBox.getNextMessage();
        		if (msg != null) {
        			JOptionPane.showMessageDialog(GuiMain.instance(), msg.getMessage(), "HID To VPAD Network Client", msg.getType());
        			MessageBox.bumpQueue();
        		}
        	}
        };
        new Timer(delay, messageBoxPerformer).start();
    }

    public static GuiMain instance() {
        return instance;
    }
}
