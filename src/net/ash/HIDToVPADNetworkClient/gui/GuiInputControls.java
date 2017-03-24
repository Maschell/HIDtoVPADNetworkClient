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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public class GuiInputControls extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static GuiInputControls instance = null;

    private static final String CONNECT = "Connect";
    private static final String DISCONNECT = "Disconnect";
    private static final String RECONNECTING = "Reconnecting";

    private JButton connectButton;
    private JTextField ipTextBox;
    private JPanel ipTextBoxWrap;
    private JTextField packetIntervalTextBox;
    private JLabel statusLabel;

    public GuiInputControls() throws Exception {
        super();
        if (instance != null) {
            throw new Exception("GuiInputControls already has an instance!");
        }
        instance = this;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(220, 150));

        connectButton = new JButton(CONNECT);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        ipTextBox = new JTextField();
        ipTextBox.setColumns(15);
        ipTextBox.setText(Settings.getIpAddr());
        ipTextBoxWrap = new JPanel(new FlowLayout());
        ipTextBoxWrap.add(new JLabel("IP: "));
        ipTextBoxWrap.add(ipTextBox);
        ipTextBoxWrap.setMaximumSize(new Dimension(1000, 20));

        statusLabel = new JLabel("Ready.");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());

        add(ipTextBoxWrap);

        add(Box.createRigidArea(new Dimension(1, 4)));
        add(connectButton);
        add(Box.createRigidArea(new Dimension(1, 8)));

        add(statusLabel);

        add(Box.createVerticalGlue());

        connectButton.addActionListener(this);

        int delay = 100; // milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (NetworkManager.getInstance().isReconnecting()) {
                    connectButton.setText(RECONNECTING);
                    connectButton.setEnabled(false);
                } else if (NetworkManager.getInstance().isConnected()) {
                    connectButton.setText(DISCONNECT);
                    connectButton.setEnabled(true);
                } else {
                    connectButton.setText(CONNECT);
                    connectButton.setEnabled(true);
                }
            }
        };
        new Timer(delay, taskPerformer).start();
    }

    public static GuiInputControls instance() {
        return instance;
    }

    public JTextField getPacketIntervalTextBox() {
        return packetIntervalTextBox;
    }

    public JTextField getIpTextBox() {
        return ipTextBox;
    }

    public JButton getConnectButton() {
        return connectButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Settings.setIpAddr(ipTextBox.getText());
                if (NetworkManager.getInstance().isReconnecting()) {

                } else {
                    if (NetworkManager.getInstance().isConnected()) {
                        NetworkManager.getInstance().disconnect();
                    } else {
                        NetworkManager.getInstance().connect(ipTextBox.getText());
                    }
                }
            }
        });

    }
}
