package net.ash.HIDToVPADNetworkClient.controller;

import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;

public class SwitchProController extends PureJavaHidController {
    public static final short SWITCH_PRO_CONTROLLER_VID = 0x57e; 
    public static final short SWITCH_PRO_CONTROLLER_PID = 0x2009; 
    
    
    public SwitchProController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        //truncate package to 11;
        this.PACKET_LENGTH = 11;
    }
    
    @Override
    public byte[] pollLatestData() {
        //remove unused data (because only changed data will be sent)
        currentData[3] = 0;
        currentData[5] = 0;
        currentData[7] = 0;
        currentData[9] = 0;
        return currentData.clone();
    }
    
    @Override
    public String getInfoText(){
        return "Switch Pro Controller on " + getIdentifier();
    }
}
