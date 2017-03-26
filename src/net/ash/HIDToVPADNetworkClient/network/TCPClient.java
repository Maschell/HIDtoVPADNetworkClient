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

    @Getter @Setter(AccessLevel.PRIVATE) private int shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;

    private String ip;

    public TCPClient() {
    }

    public synchronized void connect(String ip) throws Exception {
        sock = new Socket();
        sock.connect(new InetSocketAddress(ip, Protocol.TCP_PORT), 2000);
        in = new DataInputStream(sock.getInputStream());
        out = new DataOutputStream(sock.getOutputStream());

        HandshakeReturnCode resultHandshake = HandshakeReturnCode.GOOD_HANDSHAKE;
        if (recvByte() != Protocol.TCP_HANDSHAKE) resultHandshake = HandshakeReturnCode.BAD_HANDSHAKE;

        if (resultHandshake == HandshakeReturnCode.GOOD_HANDSHAKE) {
            ActiveControllerManager.getInstance().attachAllActiveControllers();
            this.ip = ip;
            shouldRetry = 0;
        } else {
            log.info("[TCP] Handshaking failed");
            throw new Exception();
        }
    }

    public synchronized boolean abort() {
        try {
            shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;
            sock.close();
        } catch (IOException e) {
            log.info(e.getMessage()); // TODO: handle
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
                    log.info("Trying again to connect! Attempt number " + shouldRetry);
                    connect(ip); // TODO: this is for reconnecting when the WiiU switches the application. But this breaks disconnecting, woops.
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
        return in.readByte();
    }

    public synchronized short recvShort() throws IOException {
        try {
            return in.readShort();
        } catch (IOException e) {
            log.info(e.getMessage());
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
