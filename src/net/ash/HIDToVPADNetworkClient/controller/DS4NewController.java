package net.ash.HIDToVPADNetworkClient.controller;

import java.util.Arrays;


import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.Settings;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

public class DS4NewController extends HidController {
    public static final short DS4_NEW_CONTROLLER_VID = 0x54C;
    public static final short DS4_NEW_CONTROLLER_PID = 0x09CC;

    public DS4NewController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        this.MAX_PACKET_LENGTH = 7;
    }

    @Override
    public byte[] pollLatestData() {
        byte[] currentData = super.pollLatestData();
        if(currentData.length >= 7){
            currentData = Arrays.copyOfRange(currentData, 1, 7);
        }
        return currentData;
    }

    @Override
    public String getInfoText() {
        return "DS4 on " + getIdentifier();
    }
}
