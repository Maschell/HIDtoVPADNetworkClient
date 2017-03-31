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
import lombok.Synchronized;
import net.ash.HIDToVPADNetworkClient.util.HandleFoundry;
import net.ash.HIDToVPADNetworkClient.util.Settings;

public class NetworkHIDDevice {
    @Getter private final short vid;
    @Getter private final short pid;

    @Getter @Setter private short deviceslot;
    @Getter @Setter private byte padslot;

    @Getter @Setter private boolean needFirstData = false;

    private byte[] lastdata = null;

    @Getter private final int hidHandle = HandleFoundry.next();

    private final Object readCommandLock = new Object();
    private final Object pullCommandsLock = new Object();
    private final List<DeviceCommand> commands = new ArrayList<DeviceCommand>();
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ReadCommand latestRead;

    public NetworkHIDDevice(short vid, short pid) {
        this.vid = vid;
        this.pid = pid;
    }

    @Synchronized("commands")
    private void addCommand(DeviceCommand command) {
        this.commands.add(command);
    }

    @Synchronized("commands")
    private void clearCommands() {
        this.commands.clear();
    }

    public void sendAttach() {
        addCommand(new AttachCommand(getHidHandle(), getVid(), getPid(), this));
    }

    public void sendDetach() {
        addCommand(new DetachCommand(getHidHandle(), this));
    }

    public void sendRead(byte[] data) {
        if (!Settings.SEND_DATA_ONLY_ON_CHANGE || !Arrays.equals(lastdata, data) && Settings.SEND_DATA_ONLY_ON_CHANGE || isNeedFirstData()) {
            synchronized (readCommandLock) {
                setLatestRead(new ReadCommand(getHidHandle(), data, this)); // Only get the latest Value.
            }
            lastdata = data.clone();
            if (isNeedFirstData()) {
                setNeedFirstData(false);
            }
        }
    }

    protected Collection<? extends DeviceCommand> getCommandList() {
        List<DeviceCommand> commands = new ArrayList<DeviceCommand>();
        synchronized (pullCommandsLock) {
            commands.addAll(getCommands());
            clearCommands();
        }

        synchronized (readCommandLock) {
            DeviceCommand lastRead = getLatestRead();
            if (lastRead != null) {
                commands.add(lastRead);
                setLatestRead(null);
            }
        }

        return commands;
    }

    @Synchronized("commands")
    private List<DeviceCommand> getCommands() {
        return commands;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hidHandle;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NetworkHIDDevice other = (NetworkHIDDevice) obj;
        return (hidHandle == other.hidHandle);
    }
}
