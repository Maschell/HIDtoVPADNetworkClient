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
package net.ash.HIDToVPADNetworkClient.controller;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;
import net.ash.HIDToVPADNetworkClient.util.Utilities;
import purejavahidapi.linux.UdevLibrary.hidraw_report_descriptor;

@Log
public class SwitchProController extends HidController {
    public static final short SWITCH_PRO_CONTROLLER_VID = 0x57e;
    public static final short SWITCH_PRO_CONTROLLER_PID = 0x2009;
    
    private static final int SWITCH_PRO_USB_BUTTON_A            = 0x08000000;
    private static final int SWITCH_PRO_USB_BUTTON_B            = 0x04000000;
    private static final int SWITCH_PRO_USB_BUTTON_X            = 0x02000000;
    private static final int SWITCH_PRO_USB_BUTTON_Y            = 0x01000000;
    private static final int SWITCH_PRO_USB_BUTTON_PLUS         = 0x00020000;
    private static final int SWITCH_PRO_USB_BUTTON_MINUS        = 0x00010000;
    private static final int SWITCH_PRO_USB_BUTTON_HOME         = 0x00100000;
    private static final int SWITCH_PRO_USB_BUTTON_SCREENSHOT   = 0x00200000;
    private static final int SWITCH_PRO_USB_BUTTON_R            = 0x40000000;
    private static final int SWITCH_PRO_USB_BUTTON_ZR           = 0x80000000;
    private static final int SWITCH_PRO_USB_BUTTON_STICK_R      = 0x00040000;
    private static final int SWITCH_PRO_USB_BUTTON_L            = 0x00004000;
    private static final int SWITCH_PRO_USB_BUTTON_ZL           = 0x00008000;
    private static final int SWITCH_PRO_USB_BUTTON_STICK_L      = 0x00080000;
    
    private static final byte SWITCH_PRO_USB_BUTTON_LEFT_VALUE  = 0x08;
    private static final byte SWITCH_PRO_USB_BUTTON_RIGHT_VALUE = 0x04;
    private static final byte SWITCH_PRO_USB_BUTTON_DOWN_VALUE  = 0x01;
    private static final byte SWITCH_PRO_USB_BUTTON_UP_VALUE    = 0x02;
    
   
    private static final int SWITCH_PRO_BT_BUTTON_A = 0x02000000;
    private static final int SWITCH_PRO_BT_BUTTON_B = 0x01000000;
    private static final int SWITCH_PRO_BT_BUTTON_X = 0x08000000;
    private static final int SWITCH_PRO_BT_BUTTON_Y = 0x04000000;
    
    private static final int SWITCH_PRO_BT_BUTTON_PLUS = 0x00020000;
    private static final int SWITCH_PRO_BT_BUTTON_MINUS = 0x00010000;
    private static final int SWITCH_PRO_BT_BUTTON_HOME = 0x00100000;
    
    private static final int SWITCH_PRO_BT_BUTTON_R            = 0x20000000;
    private static final int SWITCH_PRO_BT_BUTTON_ZR           = 0x80000000;
    private static final int SWITCH_PRO_BT_BUTTON_STICK_R      = 0x00080000;
    private static final int SWITCH_PRO_BT_BUTTON_L            = 0x10000000;
    private static final int SWITCH_PRO_BT_BUTTON_ZL           = 0x40000000;
    private static final int SWITCH_PRO_BT_BUTTON_STICK_L      = 0x00040000;
    
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_N_VALUE       = 0x00;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_NE_VALUE      = 0x01;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_E_VALUE       = 0x02;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_SE_VALUE      = 0x03;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_S_VALUE       = 0x04;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_SW_VALUE      = 0x05;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_W_VALUE       = 0x06;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_NW_VALUE      = 0x07;
    private static final byte SWITCH_PRO_BT_BUTTON_DPAD_NEUTRAL_VALUE = 0x08;
    
    private static boolean isUSB = false;
    
    private final byte[] CMD_readInput = new byte[]{(byte) 0x92,0x00,0x01,0x00,0x00,0x00,0x00,0x1F};
    private final byte[] CMD_getMAC = new byte[]{0x01};
    private final byte[] CMD_DO_HANDSHAKE = new byte[]{0x02};
    private final byte[] CMD_SWITCH_BAUDRATE = new byte[]{0x03};
    private final byte[] CMD_HID_ONLY = new byte[]{0x04};
    
    public SwitchProController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        // truncate package to 11;
        this.MAX_PACKET_LENGTH = 11;
                
        if((isUSB = doUSBHandshake())){
            log.info("Switch Pro Controller USB Handshake successful!");
        }else{
            log.info("Switch Pro Controller USB Handshake failed! The controller is probably connected via Bluetooth");
        }
    }

    @Override
    public byte[] pollLatestData() {
        if(isUSB){
            sendReadCmd();            
        }
       
        byte[] currentData = super.pollLatestData();
        
        if (currentData == null || currentData.length < 10) {
            return new byte[0];
        }
        if(isUSB){
            if(currentData[0] == (byte)0x81 && currentData[1] == (byte)0x92){
               
                currentData = Arrays.copyOfRange(currentData, 0x0D,0x20);
                int buttons = ByteBuffer.wrap(currentData).getInt() & 0xFFFFFF00;
                
                byte LX = (byte) ((short) ((currentData[0x04] << 8 &0xFF00) | (((short)currentData[0x03])&0xFF)) >> 0x04);
                byte LY = currentData[0x05];
                byte RX = (byte) ((short) ((currentData[0x07] << 8 &0xFF00) | (((short)currentData[0x06])&0xFF)) >> 0x04);
                byte RY = currentData[0x08];
                byte[] btData = new byte[0x0B];
                
                int newButtons = 0;                
                
                if((buttons & SWITCH_PRO_USB_BUTTON_A) == SWITCH_PRO_USB_BUTTON_A) newButtons |= SWITCH_PRO_BT_BUTTON_A;
                if((buttons & SWITCH_PRO_USB_BUTTON_B) == SWITCH_PRO_USB_BUTTON_B) newButtons |= SWITCH_PRO_BT_BUTTON_B;
                if((buttons & SWITCH_PRO_USB_BUTTON_X) == SWITCH_PRO_USB_BUTTON_X) newButtons |= SWITCH_PRO_BT_BUTTON_X;
                if((buttons & SWITCH_PRO_USB_BUTTON_Y) == SWITCH_PRO_USB_BUTTON_Y) newButtons |= SWITCH_PRO_BT_BUTTON_Y;
                
                if((buttons & SWITCH_PRO_USB_BUTTON_PLUS) == SWITCH_PRO_USB_BUTTON_PLUS) newButtons |= SWITCH_PRO_BT_BUTTON_PLUS;
                if((buttons & SWITCH_PRO_USB_BUTTON_MINUS) == SWITCH_PRO_USB_BUTTON_MINUS) newButtons |= SWITCH_PRO_BT_BUTTON_MINUS;
                if((buttons & SWITCH_PRO_USB_BUTTON_HOME) == SWITCH_PRO_USB_BUTTON_HOME) newButtons |= SWITCH_PRO_BT_BUTTON_HOME;
                //if((buttons & SWITCH_PRO_USB_BUTTON_SCREENSHOT) == SWITCH_PRO_USB_BUTTON_SCREENSHOT) newButtons |= SWITCH_PRO_BT_BUTTON_SCREENSHOT;
                
                if((buttons & SWITCH_PRO_USB_BUTTON_R) == SWITCH_PRO_USB_BUTTON_R) newButtons |= SWITCH_PRO_BT_BUTTON_R;
                if((buttons & SWITCH_PRO_USB_BUTTON_ZR) == SWITCH_PRO_USB_BUTTON_ZR) newButtons |= SWITCH_PRO_BT_BUTTON_ZR;
                if((buttons & SWITCH_PRO_USB_BUTTON_STICK_R) == SWITCH_PRO_USB_BUTTON_STICK_R) newButtons |= SWITCH_PRO_BT_BUTTON_STICK_R;
                
                if((buttons & SWITCH_PRO_USB_BUTTON_L) == SWITCH_PRO_USB_BUTTON_L) newButtons |= SWITCH_PRO_BT_BUTTON_L;
                if((buttons & SWITCH_PRO_USB_BUTTON_ZL) == SWITCH_PRO_USB_BUTTON_ZL) newButtons |= SWITCH_PRO_BT_BUTTON_ZL;
                if((buttons & SWITCH_PRO_USB_BUTTON_STICK_L) == SWITCH_PRO_USB_BUTTON_STICK_L) newButtons |= SWITCH_PRO_BT_BUTTON_STICK_L;
                
                byte dpad = currentData[2];
                byte dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_NEUTRAL_VALUE;
                
                if(((dpad & SWITCH_PRO_USB_BUTTON_UP_VALUE)           == SWITCH_PRO_USB_BUTTON_UP_VALUE) &&
                        ((dpad & SWITCH_PRO_USB_BUTTON_RIGHT_VALUE)  == SWITCH_PRO_USB_BUTTON_RIGHT_VALUE)){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_NE_VALUE;
               }else if(((dpad & SWITCH_PRO_USB_BUTTON_DOWN_VALUE)   == SWITCH_PRO_USB_BUTTON_DOWN_VALUE) &&
                        ((dpad & SWITCH_PRO_USB_BUTTON_RIGHT_VALUE)  == SWITCH_PRO_USB_BUTTON_RIGHT_VALUE)){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_SE_VALUE;
               }else if(((dpad & SWITCH_PRO_USB_BUTTON_DOWN_VALUE)   == SWITCH_PRO_USB_BUTTON_DOWN_VALUE) &&
                        ((dpad & SWITCH_PRO_USB_BUTTON_LEFT_VALUE)   == SWITCH_PRO_USB_BUTTON_LEFT_VALUE)){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_SW_VALUE;
               }else if(((dpad & SWITCH_PRO_USB_BUTTON_UP_VALUE)     == SWITCH_PRO_USB_BUTTON_UP_VALUE) &&
                        ((dpad & SWITCH_PRO_USB_BUTTON_LEFT_VALUE)   == SWITCH_PRO_USB_BUTTON_LEFT_VALUE)){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_NW_VALUE;
               }else if((dpad & SWITCH_PRO_USB_BUTTON_UP_VALUE)      == SWITCH_PRO_USB_BUTTON_UP_VALUE){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_N_VALUE;
               }else if((dpad &  SWITCH_PRO_USB_BUTTON_RIGHT_VALUE)  == SWITCH_PRO_USB_BUTTON_RIGHT_VALUE){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_E_VALUE;
               }else if((dpad &  SWITCH_PRO_USB_BUTTON_DOWN_VALUE)   == SWITCH_PRO_USB_BUTTON_DOWN_VALUE){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_S_VALUE;
               }else if((dpad &  SWITCH_PRO_USB_BUTTON_LEFT_VALUE)   == SWITCH_PRO_USB_BUTTON_LEFT_VALUE){
                   dpadResult = SWITCH_PRO_BT_BUTTON_DPAD_W_VALUE;
               }
                
                btData[0] = (byte) ((newButtons >> 24) & 0xFF);
                btData[1] = (byte) ((newButtons >> 16) & 0xFF);
                btData[2] |= dpadResult;
                btData[4] = LX;
                btData[6] = (byte) (LY * -1) ;
                btData[8] = RX;
                btData[10] = (byte) (RY * -1) ;
                currentData = btData;   
                //System.out.println(Utilities.ByteArrayToString(currentData));
            }else{
                return new byte[0];
            }
        }else{
            currentData = Arrays.copyOfRange(currentData, 0x01,12);
            currentData[3] = 0;
            currentData[5] = 0;
            currentData[7] = 0;
            currentData[9] = 0;
        }

        
        return currentData;
    }

    private boolean sendReadCmd() {
        return (hidDevice.hid_write(CMD_readInput, CMD_readInput.length,(byte) 0x80) > 0);        
    }
    
    private boolean doUSBHandshake() {
        byte[] read_buf = new byte[0x40];   
        int res = 0;     
        
        if(res >= 0){            
            hidDevice.hid_read(read_buf,read_buf.length);
           
            res = hidDevice.hid_write(CMD_DO_HANDSHAKE,CMD_DO_HANDSHAKE.length,(byte) 0x80);
            if(res < 0) return false;
                             
            res = hidDevice.hid_write(CMD_HID_ONLY,CMD_HID_ONLY.length,(byte) 0x80);
            if(res < 0) return false;
            
            res = hidDevice.hid_read(read_buf,read_buf.length+1);
            if(read_buf[0] != 0x00){
                MessageBoxManager.addMessageBox("You need to reattach the Switch Pro Controller or restart the network client!");
                return false;
            }
            return true;
           
        }
        return false;
    }

    @Override
    public String getInfoText() {
        return "Switch Pro Controller on " + getIdentifier();
    }
}
