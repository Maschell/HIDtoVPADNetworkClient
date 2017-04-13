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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.network.Protocol.HandshakeReturnCode;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

@Log
public final class NetworkManager implements Runnable {
    private final TCPClient tcpClient = TCPClient.getInstance();
    private UDPClient udpClient = null;

    @Getter private static NetworkManager instance = new NetworkManager();

    private final List<DeviceCommand> ownCommands = new ArrayList<DeviceCommand>();

    @Getter private final List<NetworkHIDDevice> devices = new ArrayList<NetworkHIDDevice>();

    /*
     * We want to remove them at the end of a cycle. To make sure the detach was send before removing.
     */
    @Getter private final List<NetworkHIDDevice> toRemove = new ArrayList<NetworkHIDDevice>();

    private NetworkManager() {
        // Singleton
    }

    public void addHIDDevice(NetworkHIDDevice device) {
        if (!devices.contains(device)) {
            synchronized (devices) {
                devices.add(device);
            }
        }
    }

    @Synchronized("toRemove")
    public void removeHIDDevice(NetworkHIDDevice device) {
        device.sendDetach();
        toRemove.add(device);
    }

    @Override
    public void run() {
        int i = 0;
        new Thread(UDPServer.getInstance(), "UDP Server").start();
        while (true) {
            proccessCommands();
            Utilities.sleep(Settings.PROCESS_CMD_INTERVAL);
            if (i++ > Settings.PING_INTERVAL / Settings.PROCESS_CMD_INTERVAL) {
                ping();
                i = 0;
            }
        }
    }

    private void ping() {
        if (isConnected() || tcpClient.isShouldRetry()) sendingCommand(new PingCommand());
    }

    private void proccessCommands() {
        List<DeviceCommand> commands = new ArrayList<DeviceCommand>();
        commands.addAll(ownCommands); // TODO: Does this need a synchronized
                                      // block? It _should_ be only access from
                                      // this thread. Need to think about it
        ownCommands.clear();
        synchronized (toRemove) {
            synchronized (devices) {
                for (NetworkHIDDevice device : getDevices()) {
                    commands.addAll(device.getCommandList());
                }
            }
        }

        if (commands.isEmpty()) return;

        // Split up into "read commands" and other commands.
        List<ReadCommand> readCommands = new ArrayList<ReadCommand>();
        {
            for (DeviceCommand command : commands) {
                if (command instanceof ReadCommand) {
                    readCommands.add((ReadCommand) command);
                }
            }
            commands.removeAll(readCommands);
        }

        if (!readCommands.isEmpty()) {
            sendingRead(readCommands);
        }

        if (!commands.isEmpty()) {
            for (DeviceCommand command : commands) {
                sendingCommand(command);
            }
        }

        synchronized (toRemove) {
            synchronized (devices) {
                for (NetworkHIDDevice d : toRemove) {
                    commands.remove(d);
                }
            }
        }
    }

    private void sendingCommand(DeviceCommand command) {
        boolean result = false;
        if (isConnected() || tcpClient.isShouldRetry()) {
            if (command instanceof AttachCommand) {
                result = sendAttach((AttachCommand) command);
            } else if (command instanceof DetachCommand) {
                result = sendDetach((DetachCommand) command);
            } else if (command instanceof PingCommand) {
                sendPing((PingCommand) command);
                result = true;
            } else {
                log.info("UNKNOWN COMMAND!");
                result = true;
            }
        } else {
            Utilities.sleep(Settings.SENDING_CMD_SLEEP_IF_NOT_CONNECTED);
        }

        // Add the command again on errors
        if (!result) {
            addCommand(command);
        }
    }

    private void sendPing(PingCommand command) {
        if (sendTCP(Protocol.getRawPingDataToSend(command))) {
            byte pong;
            try {
                pong = tcpClient.recvByte();
                if (pong == Protocol.TCP_CMD_PONG) {
                    if (Settings.DEBUG_TCP_PING_PONG) log.info("Ping...Pong!");
                } else {
                    log.info("Got no valid response to a Ping. Disconnecting.");
                    disconnect();
                }
            } catch (IOException e) {
                log.info("Failed to get PONG. Disconnecting.");
                tcpClient.checkShouldRetry();
            }

        } else {
            log.info("Sending the PING failed");
        }
    }

    private boolean sendDetach(DetachCommand command) {
        byte[] sendData;
        try {
            sendData = Protocol.getRawDetachDataToSend(command);
            if (sendTCP(sendData)) {
                log.info("Success detach command for device (" + command.getSender() + ") sent!");
            } else {
                log.info("Sending the detach command for device (" + command.getSender() + ") failed. sendTCP failed");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // TODO: Maybe move it into the Protocol class?
    private boolean sendAttach(AttachCommand command) {
        // Send the TCP command
        byte[] sendData = null;
        try {
            sendData = Protocol.getRawAttachDataToSend(command);
        } catch (IOException e) {
            log.info("Building the attach command for device (" + command.getSender() + ") failed." + e.getMessage());
            return false;
        }
        if (sendTCP(sendData)) {
            byte configFound = 0;
            try {
                configFound = recvTCPByte();
            } catch (IOException e1) {
                log.info("Failed to get byte.");
                disconnect();
                return false;
            }
            if (configFound == Protocol.TCP_CMD_ATTACH_CONFIG_FOUND) {
                // log.info("Config on the console found!");
            } else if (configFound == Protocol.TCP_CMD_ATTACH_CONFIG_NOT_FOUND) {
                log.info("NO CONFIG FOUND.");
                Controller c = ActiveControllerManager.getInstance().getControllerByHIDHandle(command.getSender().getHidHandle());
                if (c != null) {
                    c.setHasConfig(false);
                    c.setActive(false);
                }
                return true;
            } else if (configFound == 0) {
                log.info("Failed to get byte.");
                disconnect();
                return false;
            }

            byte userDataOkay = 0;
            try {
                userDataOkay = recvTCPByte();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (userDataOkay == Protocol.TCP_CMD_ATTACH_USERDATA_OKAY) {
                // log.info("userdata okay!");
            } else if (userDataOkay == Protocol.TCP_CMD_ATTACH_USERDATA_BAD) {
                log.info("USERDATA BAD.");
                return false;
            } else if (userDataOkay == 0) {
                log.info("Failed to get byte.");
                disconnect();
                return false;
            }

            // We receive our device slot and pad slot
            short deviceslot = -1;
            byte padslot = -1;
            try {
                deviceslot = recvTCPShort();
                padslot = recvTCPByte();
            } catch (IOException e) {
                log.info("Recieving data after sending a attach failed for device (" + command.getSender() + ") failed." + e.getMessage());
                return false;
            }

            if (deviceslot < 0 || padslot < 0) {
                log.info("Recieving data after sending a attach failed for device (" + command.getSender() + ") failed. We need to disconnect =(");
                disconnect();
                return false;
            }

            // Let's save them for later and demand the first data packet.
            NetworkHIDDevice sender = command.getSender();
            if (sender == null) {
                log.info("Something really went wrong. Got an attach event with out an " + NetworkHIDDevice.class.getSimpleName());
                return false;
            }

            sender.setDeviceslot(deviceslot);
            sender.setPadslot(padslot);
            sender.setNeedFirstData(true); // Please send data after connecting.

            log.info("Attaching done!");
            return true;
        } else {
            log.info("Sending the attach command for device (" + command.getSender() + ") failed. sendTCP failed");
            return false;
        }
    }

    private void sendingRead(List<ReadCommand> readCommands) {
        byte[] rawCommand;
        try {
            rawCommand = Protocol.getRawReadDataToSend(readCommands);
            if (sendUDP(rawCommand) && Settings.DEBUG_UDP_OUTPUT) {
                System.out.println("UDP Packet sent: " + Utilities.ByteArrayToString(rawCommand));
            }
        } catch (IOException e) {
            log.info("Sending read data failed.");
        }
    }

    private boolean sendUDP(byte[] rawCommand) {
        boolean result = false;
        if (udpClient != null && isConnected()) {
            try {
                udpClient.send(rawCommand);
                result = true;
            } catch (Exception e) {
                //
                result = false;
            }
        }
        return result;
    }

    private boolean sendTCP(byte[] rawCommand) {
        boolean result = false;
        if (tcpClient != null) {
            try {
                tcpClient.send(rawCommand);
                result = true;
            } catch (Exception e) {
                result = false;
            }
        }
        return result;
    }

    public void disconnect() {
        // ControllerManager.deactivateAllAttachedControllers();
        tcpClient.abort();
        udpClient = null;
    }

    private short recvTCPShort() throws IOException {
        return tcpClient.recvShort();
    }

    private byte recvTCPByte() throws IOException {
        return tcpClient.recvByte();
    }

    public boolean isConnected() {
        return (tcpClient != null && tcpClient.isConnected());
    }

    public boolean connect(String ip) {
        boolean result = false;
        log.info("Trying to connect to: " + ip);
        try {
            HandshakeReturnCode tcpresult = tcpClient.connect(ip);
            if (tcpresult == HandshakeReturnCode.GOOD_HANDSHAKE) {
                log.info("TCP Connected!");
                udpClient = UDPClient.createUDPClient(ip);
                if (udpClient != null) {
                    result = true;
                }
            } else {
                String error = "Error while connecting.";
                log.info(error);
            }
        } catch (IOException e) {
            String error = "Error while connecting: " + e.getMessage();
            log.info(error);
            MessageBoxManager.addMessageBox(error, MessageBox.MESSAGE_WARNING);
        }
        return result;
    }

    private void addCommand(DeviceCommand command) {
        this.ownCommands.add(command);
    }

    public boolean isReconnecting() {
        return !tcpClient.isConnected() && tcpClient.isShouldRetry();
    }
}
