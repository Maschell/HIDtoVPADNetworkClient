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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.network.Protocol.HandshakeReturnCode;
import net.ash.HIDToVPADNetworkClient.util.HandleFoundry;
import net.ash.HIDToVPADNetworkClient.util.Settings;

@Log
public class TCPClient {
    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;
    @Getter private int clientID = HandleFoundry.next();

    @Getter @Setter(AccessLevel.PRIVATE) private int shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;

    private String ip;

    public TCPClient() {
    }

    public synchronized void connect(String ip) throws Exception {

        sock = new Socket();
        sock.connect(new InetSocketAddress(ip, Protocol.TCP_PORT), 2000);
        in = new DataInputStream(sock.getInputStream());
        out = new DataOutputStream(sock.getOutputStream());

        HandshakeReturnCode resultHandshake = doHandshake();
        if (resultHandshake == HandshakeReturnCode.BAD_HANDSHAKE) {
            log.info("[TCP] Handshaking failed");
            throw new Exception();
        } else {
            if (resultHandshake == HandshakeReturnCode.NEW_CLIENT && this.ip != null) {
                // We check the IP to be sure it's the first time we connect to
                // a WiiU. //TODO: Sending a ID from the WiiU which will be
                // compared?
                // we are new to the client.
                ActiveControllerManager.getInstance().attachAllActiveControllers();
            } else if (resultHandshake == HandshakeReturnCode.SAME_CLIENT) {

            }
            this.ip = ip;
            shouldRetry = 0;
        }
    }

    private synchronized HandshakeReturnCode doHandshake() throws Exception {
        if (recvByte() != Protocol.TCP_HANDSHAKE)
            return HandshakeReturnCode.BAD_HANDSHAKE;
        send(clientID);
        log.info("[TCP] Handshaking...");
        HandshakeReturnCode test = (recvByte() == Protocol.TCP_NEW_CLIENT) ? HandshakeReturnCode.NEW_CLIENT : HandshakeReturnCode.SAME_CLIENT;
        return test;
    }

    public synchronized boolean abort() {
        try {
            shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;
            sock.close();
            clientID = HandleFoundry.next();
        } catch (IOException e) {
            System.out.println(e.getMessage()); // TODO: handle
            return false;
        }
        return true;
    }

    public synchronized void send(byte[] rawCommand) throws IOException {
        try {
            out.write(rawCommand);
            out.flush();
        } catch (IOException e) {
            try {
                if (shouldRetry++ < Settings.MAXIMUM_TRIES_FOR_RECONNECTING) {
                    System.out.println("Trying again to connect! Attempt number " + shouldRetry);
                    connect(ip); // TODO: this is for reconnecting when the WiiU
                                 // switches the application. But this breaks
                                 // disconnecting, woops.
                } else {
                    abort();
                }
            } catch (Exception e1) {
                // e1.printStackTrace();
            }
            throw e;
        }
    }

    public synchronized void send(int value) throws IOException {
        send(ByteBuffer.allocate(4).putInt(value).array());
    }

    public synchronized byte recvByte() throws IOException {
        try {
            return in.readByte();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public synchronized short recvShort() throws IOException {
        try {
            return in.readShort();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public synchronized boolean isConnected() {
        return (sock != null && sock.isConnected() && !sock.isClosed());
    }

    public boolean isShouldRetry() {
        return this.shouldRetry < Settings.MAXIMUM_TRIES_FOR_RECONNECTING;
    }
}
