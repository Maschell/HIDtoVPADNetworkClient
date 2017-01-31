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

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.controller.ControllerData;

public class TCPClient {
	private Socket sock;
	private DataInputStream in;
	private DataOutputStream out;
	
	public TCPClient() {
	}
	
	public synchronized void connect(String ip) throws Exception {
		sock = new Socket();
		sock.connect(new InetSocketAddress(ip, Protocol.TCP_PORT), 2000);
		in = new DataInputStream(sock.getInputStream());
		out = new DataOutputStream(sock.getOutputStream());
	}
	
	public enum HandshakeReturnCode {
		BAD_HANDSHAKE,
		SAME_CLIENT,
		NEW_CLIENT
	}
	
	public synchronized HandshakeReturnCode doHandshake() throws Exception {
		if (in.readByte() != Protocol.TCP_HANDSHAKE) return HandshakeReturnCode.BAD_HANDSHAKE;
		
		out.writeInt(NetworkManager.clientID);
		out.flush();
		System.out.println("[TCP] Handshaking...");
		return (in.readByte() == Protocol.TCP_NEW_CLIENT) ? HandshakeReturnCode.NEW_CLIENT : HandshakeReturnCode.SAME_CLIENT;
	}
	
	public synchronized void sendAttach(Controller c) throws Exception {
		System.out.println("[TCPClient] Attach " + c); //XXX debug text
		out.writeByte(Protocol.TCP_CMD_ATTACH);
		
		
		out.writeInt(c.getHandle());
		ControllerData d = c.getLatestData(); //GetLatestData allocates a new ControllerData
		out.writeShort(d.getVID());
		out.writeShort(d.getPID());
		out.flush();
		
		short deviceSlot = in.readShort();
		byte padSlot = in.readByte();
		c.setSlotData(deviceSlot, padSlot);
		
		System.out.println("Attached! deviceSlot: " + Integer.toHexString((int)deviceSlot & 0xFFFF) + " padSlot: " + Integer.toHexString((int)padSlot & 0xFF));
	}
	
	public synchronized void sendDetach(Controller c) throws Exception {
		System.out.println("[TCPClient] Detach " + c);
		out.write(Protocol.TCP_CMD_DETACH);
		
		out.writeInt(c.getHandle());
		out.flush();
	}
	
	public synchronized boolean ping() {
		//System.out.println("Ping!");
		try {
			out.writeByte(Protocol.TCP_CMD_PING);
			out.flush();
		} catch (IOException e) {
			return false;
		}
		//TODO convince Maschell to make the client actually respond to pings
		return true;
	}
	
	public synchronized void abort() throws Exception {
		sock.close();
	}
}
