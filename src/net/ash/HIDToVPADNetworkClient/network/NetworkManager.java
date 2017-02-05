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

import org.omg.Messaging.SyncScopeHelper;

import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.commands.AttachCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.DetachCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.DeviceCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.PingCommand;
import net.ash.HIDToVPADNetworkClient.network.commands.ReadCommand;
import net.ash.HIDToVPADNetworkClient.util.Utilities;

@Log
public class NetworkManager implements Runnable{    
    private final TCPClient tcpClient = new TCPClient();
    private UDPClient udpClient = null;
        
    private static NetworkManager instance = null;
    private NetworkManager() {
        
    }
    
    public static NetworkManager getInstance(){
        if(instance == null){
            instance = new NetworkManager();
        }
        return instance;
    }
    
    @Getter private List<NetworkHIDDevice> devices = new ArrayList<>();
    
    public void addHIDDevice(NetworkHIDDevice device){
        if(!getDevices().contains(device)){
            synchronized (devices) {
                getDevices().add(device);
            }
        }
    }
    
    /*
     * We want to remove them at the end of a cycle. To make sure the detach was send before removing.
     */
    @Getter private List<NetworkHIDDevice> toRemove = new ArrayList<>();
    @Synchronized("toRemove")
    public void removeHIDDevice(NetworkHIDDevice device) {
        device.sendDetach();
        toRemove.add(device);
    }

    @Override
    public void run() {
        int i = 0;
        while(true){
            proccessCommands();
            Utilities.sleep(10);
            if(i++ > 1000/10){
                ping();
                i = 0;
            }
        }
    }

    private void ping() {
        if(isConnected())sendingCommand(new PingCommand());
    }

    public void proccessCommands(){
        List<DeviceCommand> commands = new ArrayList<>();
        synchronized (toRemove) {
            synchronized (devices) {
                for(NetworkHIDDevice device : getDevices()){
                    commands.addAll(device.getCommandList());
                }
            }
        }
        
        if(commands.isEmpty())return;
        
        //Split up into "read commands" and other commands.
        List<ReadCommand> readCommands = new ArrayList<>();
        {                
            for(DeviceCommand command : commands){
                if(command instanceof ReadCommand){
                    readCommands.add((ReadCommand) command);
                }
            }
            commands.removeAll(readCommands);
        }
       
        if(!readCommands.isEmpty()){
            sendingRead(readCommands);
        }
        
        if(!commands.isEmpty()){
            for(DeviceCommand command : commands){
                sendingCommand(command);
            }
        }
        
        synchronized (toRemove) {
            synchronized (devices) {
                for(NetworkHIDDevice d: toRemove){
                    commands.remove(d);
                }
            }
        }
    }
    
    private void sendingCommand(DeviceCommand command) {
        if(command instanceof AttachCommand){
            sendAttach((AttachCommand) command);
        }else if(command instanceof DetachCommand){
            sendDetach((DetachCommand) command);
        }else if(command instanceof PingCommand){
            sendPing((PingCommand) command);
        }
    }

    private void sendPing(PingCommand command) {
        if(sendTCP(Protocol.getRawPingDataToSend(command))){
            log.info("PING");
        }else{
            log.info("Sending the PING failed");
        }
    }

    private void sendDetach(DetachCommand command) {
        byte[] sendData;
        try {
            sendData = Protocol.getRawDetachDataToSend(command);
            if(!sendTCP(sendData)){
                log.info("Sending the detach command for device (" + command.getSender() + ") failed. sendTCP failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
        
    }
   
    private void sendAttach(AttachCommand command) {
        //Send the TCP command
        byte[] sendData = null;
        try {
            sendData = Protocol.getRawAttachDataToSend(command);
        }catch (IOException e) {
            log.info("Building the attach command for device (" + command.getSender() + ") failed." + e.getMessage());
            return;
        } 
        if(sendTCP(sendData)){
            byte configFound = 0;
            try {
                configFound = recvTCPByte();                
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if(configFound == Protocol.TCP_CMD_ATTACH_CONFIG_FOUND){
                log.info("Config on the console found!");
            }else if(configFound == Protocol.TCP_CMD_ATTACH_CONFIG_NOT_FOUND){
                log.info("NO CONFIG FOUND.");
                return;
            }else if (configFound == 0){
                log.info("Failed to get byte.");
                disconnect();
            }
            
            byte userDataOkay = 0;
            try {
                userDataOkay = recvTCPByte();                
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if(userDataOkay == Protocol.TCP_CMD_ATTACH_USERDATA_OKAY){
                log.info("userdata okay!");
            }else if(userDataOkay == Protocol.TCP_CMD_ATTACH_USERDATA_BAD){
                log.info("USERDATA BAD.");
                return;
            }else if (userDataOkay == 0){
                log.info("Failed to get byte.");
                disconnect();
            }
            
            //We receive our device slot and pad slot
            short deviceslot = -1;
            byte padslot = -1;
            try {
                deviceslot = recvTCPShort();
                padslot = recvTCPByte();
            } catch (IOException e) {
                log.info("Recieving data after sending a attach failed for device (" + command.getSender() + ") failed." + e.getMessage());
            }
            
            if(deviceslot < 0 || padslot < 0){
                log.info("Recieving data after sending a attach failed for device (" + command.getSender() + ") failed. We need to disconnect =(");
                disconnect();
            }
            
            //Let's save them for later.
            NetworkHIDDevice sender  = command.getSender();
            if(sender != null){
                sender.setDeviceslot(deviceslot);
                sender.setPadslot(padslot);
            }else{
                log.info("Something really went wrong. Got an attach event with out an " + NetworkHIDDevice.class.getSimpleName());
            }
            log.info("Attaching done!");
        }else{
            log.info("Sending the attach command for device (" + command.getSender() + ") failed. sendTCP failed");
        }
    }
    
    private void sendingRead(List<ReadCommand> readCommands) {
        byte[] rawCommand;
        try {
            rawCommand = Protocol.getRawReadDataToSend(readCommands);
            System.out.println("UDP Packet: "+ Utilities.ByteArrayToString(rawCommand));
            sendUDP(rawCommand);
        } catch (IOException e) {
            System.out.println("Sending read data failed.");
        }
    }
    
    private boolean sendUDP(byte[] rawCommand) {
        boolean result = false;
        if(udpClient != null){
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
        if(tcpClient != null){
            try {
                tcpClient.send(rawCommand);
                result = true;
            } catch (Exception e) {
                //
                result = false;
            }
        }
        return result;
    }
    
    public void disconnect() {
        ControllerManager.deactivateAllAttachedControllers();
        tcpClient.abort();
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
            tcpClient.connect(ip);
            System.out.println("TCP Connected!");
            udpClient = UDPClient.createUDPClient(ip);
            if(udpClient != null){
                result = true;
            }
        } catch (Exception e) {
            System.out.println("Error while connecting: " + e.getMessage());
        }
        return result;
    }
}
