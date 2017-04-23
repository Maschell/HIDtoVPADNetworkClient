package de.mas.HIDTest;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;
import net.ash.HIDToVPADNetworkClient.util.Settings.ControllerFiltering;
import net.ash.HIDToVPADNetworkClient.util.Settings.ControllerFiltering.Type;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

public class Main {

    public static void main(String[] args) {
        AutoRunFromConsole.runYourselfInConsole(true);

        Settings.AUTO_ACTIVATE_CONTROLLER = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ControllerManager.detectControllers();
                    Utilities.sleep(Settings.DETECT_CONTROLLER_INTERVAL);
                }
            }
        }).start();

        System.out.println("Scanning for controllers...");

        Utilities.sleep(1000);

        for (Controller c : ControllerManager.getAttachedControllers()) {
            c.setActive(true);
        }

        Utilities.sleep(1000);

        Settings.AUTO_ACTIVATE_CONTROLLER = true;
        ControllerFiltering.setFilterState(Type.HIDGAMEPAD, true);
        ControllerFiltering.setFilterState(Type.XINPUT, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    boolean attached = false;
                    for (Controller c : ControllerManager.getAttachedControllers()) {
                        if (c.isActive()) {
                            attached = true;
                            System.out.print(String.format("VID: %04X PID %04X", c.getVID(), c.getPID()) + " data: "
                                    + Utilities.ByteArrayToString(c.getLatestData()) + " | ");
                        }
                    }
                    if (attached) System.out.print("\r");
                    Utilities.sleep(15);
                }
            }
        }).start();
    }
}
