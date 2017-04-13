package net.ash.HIDToVPADNetworkClient.util;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.hid.HidDevice;
import net.ash.HIDToVPADNetworkClient.hid.HidManager;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

public class StatusReport {
    public static String generateStatusReport() {
        String report = "HID to VPAD Network Client\n\nRunning on ";
        report += Settings.getPlattform();
        
        report += "\nHID Backend: ";
        report += HidManager.getBackendType();
        
        report += "\nCurrently ";
        report += (NetworkManager.getInstance().isConnected()) ? "Connected.\n" : "Disconnected.\n";
        report += (NetworkManager.getInstance().isReconnecting()) ? "" : "Not ";
        report += "Reconnecting.";
        
        report += "\n\nCurrently attached controllers:";
        for (Controller c : ControllerManager.getAttachedControllers()) {
            report += "\n";
            report += c.toString();
        }
        
        report += "\n\nFiltering settings:\n";
        report += Settings.ControllerFiltering.getFilterStates();
        
        report += "\n\nAll HIDs:";
        for (HidDevice d : HidManager.getAllAttachedControllers()) {
            report += "\n";
            report += d.toString();
        }
        
        return report;
    }
}
