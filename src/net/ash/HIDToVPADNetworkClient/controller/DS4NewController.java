package net.ash.HIDToVPADNetworkClient.controller;

import java.util.Arrays;

import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public class DS4NewController extends HidController {
    public static final short DS4_NEW_CONTROLLER_VID = 0x54C;
    public static final short DS4_NEW_CONTROLLER_PID = 0x09CC;

    public DS4NewController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        if (Settings.isMacOSX()) {
            this.MAX_PACKET_LENGTH = 7;
        } else {
            this.MAX_PACKET_LENGTH = 6;
        }
    }

    @Override
    public byte[] pollLatestData() {
        byte[] currentData = super.pollLatestData();
        if (Settings.isMacOSX() && currentData != null && currentData.length > 6) { // for some reason the controller has one extra byte at the beginning under
                                                                                    // OSX
            currentData = Arrays.copyOfRange(currentData, 1, 7);
        }
        return currentData;
    }

    @Override
    public String getInfoText() {
        return "DS4 on " + getIdentifier();
    }
}
