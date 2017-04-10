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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;

@Log
final class Protocol {
    private static ProtocolVersion currentProtocol = ProtocolVersion.MY_VERSION;
    static final int TCP_PORT = 8112;
    static final int UDP_PORT = 8113;

    static final byte TCP_HANDSHAKE_VERSION_1 = 0x12;
    static final byte TCP_HANDSHAKE_VERSION_2 = 0x13;
    static final byte TCP_HANDSHAKE_ABORT = 0x30;

    static final byte TCP_HANDSHAKE = TCP_HANDSHAKE_VERSION_2; // default version.

    static final byte TCP_SAME_CLIENT = 0x20;
    static final byte TCP_NEW_CLIENT = 0x21;

    static final byte TCP_CMD_ATTACH = 0x01;
    static final byte TCP_CMD_DETACH = 0x02;
    static final byte TCP_CMD_PING = (byte) 0xF0;
    static final byte TCP_CMD_PONG = (byte) 0xF1;

    static final byte UDP_CMD_DATA = 0x03;

    static final byte TCP_CMD_ATTACH_CONFIG_FOUND = (byte) 0xE0;
    static final byte TCP_CMD_ATTACH_CONFIG_NOT_FOUND = (byte) 0xE1;
    static final byte TCP_CMD_ATTACH_USERDATA_OKAY = (byte) 0xE8;
    static final byte TCP_CMD_ATTACH_USERDATA_BAD = (byte) 0xE9;

    private Protocol() {
    }

    enum HandshakeReturnCode {
        GOOD_HANDSHAKE, BAD_HANDSHAKE
    }

    static byte[] getRawAttachDataToSend(AttachCommand command) throws IOException {
        return ByteBuffer.allocate(9).put(Protocol.TCP_CMD_ATTACH).putInt(command.getHandle()).putShort(command.getVid()).putShort(command.getPid()).array();
    }

    static byte[] getRawDetachDataToSend(DetachCommand command) throws IOException {
        return ByteBuffer.allocate(5).put(Protocol.TCP_CMD_DETACH).putInt(command.getHandle()).array();
    }

    static byte[] getRawPingDataToSend(PingCommand command) {
        return new byte[] { Protocol.TCP_CMD_PING };
    }

    static byte[] getRawReadDataToSend(List<ReadCommand> readCommands) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(Protocol.UDP_CMD_DATA);
        dos.writeByte(readCommands.size());

        for (ReadCommand command : readCommands) {
            NetworkHIDDevice sender = command.getSender();
            byte[] data = command.getData();
            if (data.length > 0xFF) {
                log.info("Tried to send too much data. Maximum is 0xFF bytes read command.");
                continue;
            }

            byte newLength = (byte) (data.length & 0xFF);

            dos.writeInt(command.getHandle());
            dos.writeShort(sender.getDeviceslot());
            dos.writeByte(sender.getPadslot());

            dos.write(newLength);
            dos.write(data, 0, newLength);
        }

        return bos.toByteArray();
    }

    public static HandshakeReturnCode doHandshake(TCPClient tcpClient) throws IOException {
        Protocol.currentProtocol = ProtocolVersion.MY_VERSION;
        HandshakeReturnCode resultHandshake = HandshakeReturnCode.BAD_HANDSHAKE;
        ProtocolVersion wiiuProtocolVersion = ProtocolVersion.getEnumByValue(tcpClient.recvByte());

        /**
         * Handshake 1. At first the server (WiiU) sends his protocol version 2. The network clients answers with his preferred version (which needs to be
         * equals or lower the version the server (WiiU) sent him) or an abort command. 3a. If the client sent a abort, close the connection and wait for
         * another connection 3b. If the client sent his highest supported version, the server confirm that he is able to use this version (by sending the
         * version back) or sending a abort command to disconnect.
         **/

        if (wiiuProtocolVersion == ProtocolVersion.UNKOWN) {
            log.info("Connected to a WiiU with an unsupported protocol version. It may support an older though");
            tcpClient.send(ProtocolVersion.MY_VERSION.getVersionByte());
        } else if (wiiuProtocolVersion == ProtocolVersion.Version1) {
            // Do nothing.
        } else if (wiiuProtocolVersion == ProtocolVersion.Version2) {
            // We want to do version 2!
            tcpClient.send(ProtocolVersion.Version2.getVersionByte());
        }

        if (wiiuProtocolVersion == ProtocolVersion.Version1) {
            Protocol.currentProtocol = ProtocolVersion.Version1;
            resultHandshake = HandshakeReturnCode.GOOD_HANDSHAKE;
        } else {
            ProtocolVersion finalProtocolVersion = ProtocolVersion.getEnumByValue(tcpClient.recvByte());
            if (finalProtocolVersion == ProtocolVersion.Abort) {
                String message = "The HIDtoVPAD version is not supported. Try to use the newest version of HIDtoVPAD and this network client.";
                log.info(message);
                MessageBoxManager.addMessageBox(message, MessageBox.MESSAGE_ERROR);
                resultHandshake = HandshakeReturnCode.BAD_HANDSHAKE;
            } else if (finalProtocolVersion == ProtocolVersion.UNKOWN) {
                String message = "Something stranged happend while connecting. Try to use the newest version of HIDtoVPAD and this network client.";
                log.info(message);
                MessageBoxManager.addMessageBox(message, MessageBox.MESSAGE_ERROR);
                resultHandshake = HandshakeReturnCode.BAD_HANDSHAKE;
            } else {
                Protocol.currentProtocol = finalProtocolVersion;
                resultHandshake = HandshakeReturnCode.GOOD_HANDSHAKE;
            }
        }

        if (resultHandshake == HandshakeReturnCode.GOOD_HANDSHAKE) {
            ActiveControllerManager.getInstance().attachAllActiveControllers();
            log.info("Handshake was successful! Using protocol version " + (currentProtocol.getVersionByte() - TCP_HANDSHAKE_VERSION_1 + 1));
            return resultHandshake;
        } else {
            log.info("[TCP] Handshaking failed");
            return resultHandshake;
        }
        // return false;
    }

    public enum ProtocolVersion {
        MY_VERSION((byte) TCP_HANDSHAKE), Version1((byte) TCP_HANDSHAKE_VERSION_1), Version2((byte) TCP_HANDSHAKE_VERSION_2), Abort(
                (byte) TCP_HANDSHAKE_ABORT), UNKOWN((byte) 0x00);
        private final byte versionByte;

        private ProtocolVersion(byte versionByte) {
            this.versionByte = versionByte;
        }

        public static ProtocolVersion getEnumByValue(byte value) {
            switch (value) {
            case TCP_HANDSHAKE_VERSION_1:
                return ProtocolVersion.Version1;
            case TCP_HANDSHAKE_VERSION_2:
                return ProtocolVersion.Version2;
            case TCP_HANDSHAKE_ABORT:
                return ProtocolVersion.Abort;
            default:
                return ProtocolVersion.UNKOWN;
            }
        }

        public byte getVersionByte() {
            return versionByte;
        }
    }

}
