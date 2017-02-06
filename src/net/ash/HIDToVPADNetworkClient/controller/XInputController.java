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

import com.ivan.xinput.XInputAxes;
import com.ivan.xinput.XInputButtons;
import com.ivan.xinput.XInputComponents;
import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.exceptions.XInputNotLoadedException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

public class XInputController extends Controller {
    //the pad number will be appended to this String.
    public static final String XINPUT_INDENTIFER = "\\\\?\\XINPUT\\";
    
    @Getter @Setter(AccessLevel.PRIVATE) private XInputDevice device;
    
    public XInputController(ControllerType type, String identifier) throws ControllerInitializationFailedException {
        super(type, identifier);
    }
    
    @Override
    public boolean initController(String identifier) {
        int pad = Integer.parseInt(identifier.substring(XINPUT_INDENTIFER.length()));
        XInputDevice device = null;
        try {
            device = XInputDevice.getDeviceFor(pad);
        } catch (XInputNotLoadedException e) {
            //Log?
        }
        if(device == null) return false;
        setDevice(device);
        return true;
    }

    @Override
    public byte[] pollLatestData() {
        if(device.poll()){
           ByteBuffer data = ByteBuffer.allocate(8);
           XInputComponents components = device.getComponents();
           
           XInputButtons buttons = components.getButtons();
                      
           int buttonState = 0;
           if(buttons.a) buttonState |= (1 << 0); 
           if(buttons.b) buttonState |= (1 << 1);
           if(buttons.x) buttonState |= (1 << 2);
           if(buttons.y) buttonState |= (1 << 3);
           
           if(buttons.left) buttonState |= (1 << 4);
           if(buttons.up) buttonState |= (1 << 5);
           if(buttons.right) buttonState |= (1 << 6);
           if(buttons.down) buttonState |= (1 << 7);
           
           if(buttons.back) buttonState |= (1 << 8);
           if(buttons.start) buttonState |= (1 << 9);
           if(buttons.lShoulder) buttonState |= (1 << 10);
           if(buttons.rShoulder) buttonState |= (1 << 11);
           if(buttons.lThumb) buttonState |= (1 << 12);
           if(buttons.rThumb) buttonState |= (1 << 13);
           if(buttons.unknown) buttonState |= (1 << 14);
           if (XInputDevice.isGuideButtonSupported()) {
               if (buttons.guide) buttonState |= (1 << 15);
           }

           XInputAxes axes = components.getAxes();
           int axesData = 0;
           
           axesData |= Utilities.signedShortToByte(axes.lxRaw) << 24;
           axesData |= Utilities.signedShortToByte(axes.lyRaw) << 16;
           axesData |= Utilities.signedShortToByte(axes.rxRaw) << 8;
           axesData |= Utilities.signedShortToByte(axes.ryRaw) << 0;
           
           short axesDataShoulderButtons = 0;
           
           axesDataShoulderButtons |= axes.ltRaw << 8;
           axesDataShoulderButtons |= axes.rtRaw << 0;
           
           buttonState |= axesDataShoulderButtons << 16;
           data.putInt(axesData).putInt(buttonState);
           
           return(data.array());
       }
       return null;
    }

    @Override
    public void destroyDriver() {
        //not needed
    }

    //TODO: Other values for VID/PID? I guess other people had this idea too...
    @Override
    public short getVID() {       
        return 0x7331;
    }

    @Override
    public short getPID() {
        return  0x1337;
    }
}