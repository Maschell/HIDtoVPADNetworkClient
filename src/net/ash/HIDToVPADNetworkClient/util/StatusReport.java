package net.ash.HIDToVPADNetworkClient.util;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.hid.HidDevice;
import net.ash.HIDToVPADNetworkClient.hid.HidManager;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;

public class StatusReport {
    public static String generateStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("HID to VPAD Network Client\n\nRunning on ");
        report.append(Settings.getPlattform());

        report.append(System.lineSeparator()).append("HID Backend: ");
        report.append(HidManager.getBackendType());

        report.append(System.lineSeparator()).append("Currently ");
        report.append((NetworkManager.getInstance().isConnected()) ? "Connected.\n" : "Disconnected.").append(System.lineSeparator());
        report.append((NetworkManager.getInstance().isReconnecting()) ? "" : "Not ");
        report.append("Reconnecting.");

        report.append(System.lineSeparator()).append(System.lineSeparator()).append("Currently attached controllers:");
        for (Controller c : ControllerManager.getAttachedControllers()) {
            report.append(System.lineSeparator());
            report.append(c.toString());
        }

        report.append(System.lineSeparator()).append(System.lineSeparator()).append("Filtering settings:").append(System.lineSeparator());
        report.append(Settings.ControllerFiltering.getFilterStates());

        report.append(System.lineSeparator()).append(System.lineSeparator()).append("All HIDs:");
        for (HidDevice d : HidManager.getAllAttachedControllers()) {
            report.append(System.lineSeparator());
            report.append(d.toString());
        }

        return report.toString();
    }
}
