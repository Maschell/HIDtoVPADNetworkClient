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

package net.ash.HIDToVPADNetworkClient.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ash.HIDToVPADNetworkClient.network.commands.AttachCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.DetachCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.DeviceCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.ReadCommand;
import net.ash.HIDToVPADNetworkClient.util.HandleFoundry;

public class NetworkHIDDevice {
    @Getter private final short vid;
    @Getter private final short pid;
    
    @Getter @Setter private short deviceslot;
    @Getter @Setter private byte padslot;
    
    @Getter private int hidHandle = HandleFoundry.next();
    @Getter(AccessLevel.PRIVATE) private List<DeviceCommand> commands = new ArrayList<>();
    
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ReadCommand latestRead;
    
    private Object readCommandLock = new Object();
    
    public NetworkHIDDevice(short vid, short pid){
        this.vid = vid;
        this.pid = pid;
    }
    
    private void addCommand(DeviceCommand command){
        this.commands.add(command);
    }
    
    private void clearCommands(){
        this.commands.clear();
    }
    
    public void sendAttach(){
        addCommand(new AttachCommand(getHidHandle(), getVid(), getPid(),this));
    }
    
    public void sendDetach(){
        addCommand(new DetachCommand(getHidHandle(),this));
    }
    
    private byte[] lastdata = null;
    public void sendRead(byte[] data){
        if(!Arrays.equals(lastdata, data)){
            synchronized (readCommandLock) {
                setLatestRead(new ReadCommand(getHidHandle(),data, this)); //Only get the latest Value.
            }
            lastdata = data.clone();
        }
    }
    
    public Collection<? extends DeviceCommand> getCommandList() {
        List<DeviceCommand> commands = new ArrayList<>();
        commands.addAll(getCommands());
        DeviceCommand lastRead;
        
        synchronized (readCommandLock) {
            if((lastRead = getLatestRead()) != null){
                commands.add(lastRead);
                setLatestRead(null);
            }
        }
        
        clearCommands();        
        return commands;
    }
}