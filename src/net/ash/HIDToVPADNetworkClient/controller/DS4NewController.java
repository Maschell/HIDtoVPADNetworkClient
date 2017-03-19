package net.ash.HIDToVPADNetworkClient.controller;

import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;

public class DS4NewController extends PureJavaHidController {
    public static final short DS4_NEW_CONTROLLER_VID = 0x54C; 
    public static final short DS4_NEW_CONTROLLER_PID = 0x09CC; 
    
    public DS4NewController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        //truncate package to 6;
        this.PACKET_LENGTH = 6;
    }
    
    @Override
    public byte[] pollLatestData() {
        return currentData.clone();
    }
    
    @Override
    public String getInfoText(){
        return "DS4 on " + getIdentifier();
    }
}
