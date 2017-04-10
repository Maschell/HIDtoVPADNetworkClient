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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import lombok.Getter;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public final class GuiInputControls extends JPanel {
    private static final long serialVersionUID = 1L;
    @Getter private static GuiInputControls instance = new GuiInputControls();

    private static final String CONNECT = "Connect";
    private static final String DISCONNECT = "Disconnect";
    private static final String RECONNECTING = "Reconnecting";
    private final JTextField ipTextBox;

    private GuiInputControls() {
        super();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(220, 200));

        final JButton connectButton = new JButton(CONNECT);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JCheckBox cbautoScanForController = new JCheckBox();
        cbautoScanForController.setSelected(Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS);

        cbautoScanForController.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBox) e.getSource()).isSelected();
                Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS = selected;
                cbautoScanForController.setSelected(selected);
            }
        });

        final JButton scanButton = new JButton("Scan for Controllers");
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ControllerManager.detectControllers();
                    }
                });
            }
        });

        JPanel scanWrap = new JPanel();
        scanWrap.setLayout(new BoxLayout(scanWrap, BoxLayout.X_AXIS));
        JLabel label = new JLabel("Auto Scan for Controllers: ");
        scanWrap.add(label);
        scanWrap.add(cbautoScanForController);

        ipTextBox = new JTextField();
        ipTextBox.setColumns(15);
        ipTextBox.setText(Settings.getIpAddr());
        JPanel ipTextBoxWrap = new JPanel(new FlowLayout());
        ipTextBoxWrap.add(new JLabel("IP: "));
        ipTextBoxWrap.add(ipTextBox);
        ipTextBoxWrap.setMaximumSize(new Dimension(1000, 20));

        final JCheckBox cbautoActivateController = new JCheckBox();
        cbautoActivateController.setSelected(Settings.AUTO_ACTIVATE_CONTROLLER);
        cbautoActivateController.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBox) e.getSource()).isSelected();
                Settings.AUTO_ACTIVATE_CONTROLLER = selected;
                cbautoActivateController.setSelected(selected);
            }
        });

        JPanel autoActivateWrap = new JPanel();
        autoActivateWrap.setLayout(new BoxLayout(autoActivateWrap, BoxLayout.X_AXIS));
        autoActivateWrap.add(new JLabel("Auto Activate Controller: "));
        autoActivateWrap.add(cbautoActivateController);

        add(Box.createVerticalGlue());

        add(ipTextBoxWrap);

        add(Box.createRigidArea(new Dimension(1, 4)));
        add(connectButton);
        add(Box.createRigidArea(new Dimension(1, 4)));
        add(scanButton);
        add(Box.createRigidArea(new Dimension(1, 4)));
        add(scanWrap);
        add(Box.createRigidArea(new Dimension(1, 4)));
        add(autoActivateWrap);
        add(Box.createVerticalGlue());

        add(Box.createVerticalGlue());

        connectButton.addActionListener(new ActionListener() {
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
        });

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
}
