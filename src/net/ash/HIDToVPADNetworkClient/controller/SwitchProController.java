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

import net.ash.HIDToVPADNetworkClient.exeption.ControllerInitializationFailedException;

public class SwitchProController extends HidController {
    public static final short SWITCH_PRO_CONTROLLER_VID = 0x57e;
    public static final short SWITCH_PRO_CONTROLLER_PID = 0x2009;

    public SwitchProController(String identifier) throws ControllerInitializationFailedException {
        super(identifier);
        // truncate package to 11;
        this.MAX_PACKET_LENGTH = 11;
    }

    @Override
    public byte[] pollLatestData() {
        byte[] currentData = super.pollLatestData();
        if (currentData == null || currentData.length < 10) {
            return new byte[0];
        }
        // remove unused data (because only changed data will be sent)
        currentData[3] = 0;
        currentData[5] = 0;
        currentData[7] = 0;
        currentData[9] = 0;
        return currentData;
    }

    @Override
    public String getInfoText() {
        return "Switch Pro Controller on " + getIdentifier();
    }
}
