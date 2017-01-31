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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.controller.ControllerData;

public class UDPClient {
	private DatagramSocket sock;
	private InetAddress host;
	
	private ByteArrayOutputStream outBytes;
	private DataOutputStream out;
	
	public UDPClient() {
		outBytes = new ByteArrayOutputStream();
		out = new DataOutputStream(outBytes);
	}
	
	public void connect(String ip) throws Exception {
		sock = new DatagramSocket();
		host = InetAddress.getByName(ip);
	}
	
	public void send(HashMap<Integer, Controller> controllers) throws Exception {
		out.writeByte(Protocol.UDP_CMD_DATA);
		
		out.writeByte((byte)(controllers.size() & 0xFF));
		
		for (Controller c : controllers.values()) {
			out.writeInt(c.getHandle());
			out.writeShort(c.getDeviceSlot());
			out.writeByte(c.getPadSlot());
			
			ControllerData d = c.getLatestData();
			try {
				out.writeInt(d.getData().length);
				out.write(d.getData(), 0, d.getData().length);
			} catch (NullPointerException e) {
				out.writeInt(1);
				out.writeByte(0x00);
			}
		}
		
		out.flush();
		byte[] payload = outBytes.toByteArray();
		DatagramPacket packet = new DatagramPacket(payload, payload.length, host, Protocol.UDP_PORT);
		
		sock.send(packet);
		
		//System.out.println(Arrays.toString(payload)); //XXX debug text
		
		outBytes.reset();
	}
}
