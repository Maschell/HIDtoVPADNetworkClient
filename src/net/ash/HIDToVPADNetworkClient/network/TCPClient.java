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
import lombok.Synchronized;
import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.network.Protocol.HandshakeReturnCode;
import net.ash.HIDToVPADNetworkClient.util.Settings;

@Log
final class TCPClient {
    private final Object lock = new Object();
    @Getter private static TCPClient instance = new TCPClient();
    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;

    @Getter @Setter(AccessLevel.PRIVATE) private int shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;

    private String ip;

    private TCPClient() {
    }

    @Synchronized("lock")
    HandshakeReturnCode connect(String ip) throws IOException {
        sock = new Socket();
        sock.connect(new InetSocketAddress(ip, Protocol.TCP_PORT), 2000);
        in = new DataInputStream(sock.getInputStream());
        out = new DataOutputStream(sock.getOutputStream());

        HandshakeReturnCode result = Protocol.doHandshake(this);
        if (result == HandshakeReturnCode.GOOD_HANDSHAKE) {
            this.ip = ip;
            shouldRetry = 0;
        }
        return result;
    }

    @Synchronized("lock")
    boolean abort() {
        try {
            shouldRetry = Settings.MAXIMUM_TRIES_FOR_RECONNECTING;
            sock.close();
        } catch (IOException e) {
            log.info(e.getMessage()); // TODO: handle
            return false;
        }
        return true;
    }

    @Synchronized("lock")
    void send(byte[] rawCommand) throws IOException {
        try {
            out.write(rawCommand);
            out.flush();
        } catch (IOException e) {
            checkShouldRetry();
            throw e;
        }
    }

    protected void checkShouldRetry() {
        try {
            if (shouldRetry++ < Settings.MAXIMUM_TRIES_FOR_RECONNECTING) {
                log.info("Trying again to connect! Attempt number " + shouldRetry);
                connect(ip);
            } else {
                abort();
            }
        } catch (Exception e1) {
            // e1.printStackTrace();
        }
    }

    void send(int value) throws IOException {
        send(ByteBuffer.allocate(4).putInt(value).array());
    }

    public void send(byte _byte) throws IOException {
        send(ByteBuffer.allocate(1).put(_byte).array());
    }

    @Synchronized("lock")
    byte recvByte() throws IOException {
        return in.readByte();
    }

    @Synchronized("lock")
    short recvShort() throws IOException {
        try {
            return in.readShort();
        } catch (IOException e) {
            log.info(e.getMessage());
            throw e;
        }
    }

    @Synchronized("lock")
    boolean isConnected() {
        return (sock != null && sock.isConnected() && !sock.isClosed());
    }

    boolean isShouldRetry() {
        return this.shouldRetry < Settings.MAXIMUM_TRIES_FOR_RECONNECTING;
    }
}
