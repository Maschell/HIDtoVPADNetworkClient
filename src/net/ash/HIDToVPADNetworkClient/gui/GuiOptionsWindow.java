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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.util.Settings;

@Log
public class GuiOptionsWindow extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final GuiOptionsWindow instance = new GuiOptionsWindow();
    
    private final List<Tab> tabs = new ArrayList<Tab>();
    
    public static void showWindow() {
        showWindow(null);
    }
    
    public static void showWindow(Component parent) {
        instance.setOpaque(true);
        for (Tab t : instance.tabs) {
            t.updateTab();
        }
        
        JFrame window = new JFrame("Options");
        //TODO: close window behaviour
        window.setContentPane(instance);
        window.pack();
        window.setLocationRelativeTo(parent);
        window.setVisible(true);
    }
    
    private GuiOptionsWindow() {
        super(new GridLayout(1, 1));
        
        log.info("Hello from the Options window!");
        
        setPreferredSize(new Dimension(600, 300));
        
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        Tab controllerTab = new ControllerTab();
        tabs.add(controllerTab);
        tabPane.addTab("Controllers", controllerTab);
        
        Tab infoTab = new InfoTab();
        tabs.add(infoTab);
        tabPane.addTab("Info", infoTab);
        
        add(tabPane);
    }

    private class ControllerTab extends Tab {
        private static final long serialVersionUID = 1L;
        
        private final ControllerFilteringList cFilterList;
        
        private ControllerTab() {
            super(new GridLayout(1, 2));
            
            cFilterList = new ControllerFilteringList();
            
            for (Settings.ControllerFiltering.Type type : Settings.ControllerFiltering.Type.values()) {
                ControllerFilteringListItem item = new ControllerFilteringListItem(type);
                cFilterList.add(item);
            }
            
            add(cFilterList);
            
            add(new JPanel()); //TODO right side controls
        }
        
        @Override
        public void updateTab() {
            for (ControllerFilteringListItem c : cFilterList.items) {
                c.updateItem();
            }
        }
        
        private class ControllerFilteringList extends JPanel {
            private static final long serialVersionUID = 1L;
            
            private List<ControllerFilteringListItem> items = new ArrayList<ControllerFilteringListItem>();
            private JPanel innerPanel;
            
            private ControllerFilteringList() {
                super(new BorderLayout());
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
                
                innerPanel = new JPanel();
                innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));
                
                JScrollPane innerPanelWrap = new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                innerPanelWrap.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                add(innerPanelWrap, BorderLayout.CENTER);
                
                JLabel controllerFilterText = new JLabel("Controllers to show:");
                controllerFilterText.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                add(controllerFilterText, BorderLayout.PAGE_START);
            }
            
            public Component add(ControllerFilteringListItem c) {
                items.add(c);
                return innerPanel.add(c);
            }
        }
        
        private class ControllerFilteringListItem extends JPanel {
            private static final long serialVersionUID = 1L;
            
            private final JCheckBox cBox;
            private final Settings.ControllerFiltering.Type type;
            
            private ControllerFilteringListItem(Settings.ControllerFiltering.Type typeIn) {
                super(new GridLayout(1, 1));
                this.type = typeIn;
                
                cBox = new JCheckBox(type.getName());
                cBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                cBox.setSelected(Settings.ControllerFiltering.getFilterState(type));
                cBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Settings.ControllerFiltering.setFilterState(type, cBox.isSelected());
                    }
                });
                add(cBox);
            }
            
            public void updateItem() {
                cBox.setSelected(Settings.ControllerFiltering.getFilterState(type));
            }
            
            //I can't believe I didn't figure this out for GuiControllerList
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        }
    }
    
    private class InfoTab extends Tab {
        private static final long serialVersionUID = 1L;
        
        private final JTextArea infoText;
        
        private InfoTab() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            
            infoText = new JTextArea();
            infoText.setBorder(BorderFactory.createLoweredBevelBorder());
            infoText.setEditable(false);
            infoText.setText("WIP");
            JPanel infoTextWrap = new JPanel(new GridLayout(1, 1));
            infoTextWrap.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            infoTextWrap.add(infoText);
            infoTextWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(infoTextWrap);
            
            JButton copyButton = new JButton("Copy");
            copyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringSelection data = new StringSelection(infoText.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
                }
            });
            copyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(copyButton);
            add(Box.createVerticalStrut(10));
        }

        @Override
        public void updateTab() {
            //TODO update info text
        }
    }
    
    private abstract class Tab extends JPanel {
        private static final long serialVersionUID = 1L;
        public abstract void updateTab();
        public Tab(LayoutManager l) {
            super(l);
        }
        public Tab() {
            super();
        }
    }
}
