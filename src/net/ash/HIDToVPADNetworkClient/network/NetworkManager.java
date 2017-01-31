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

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.gui.GuiInteractionManager;

public class NetworkManager implements Runnable {
	private static final int PING_INTERVAL = 1000;
	
	public static int clientID;
	
	private static NetworkManager instance = null;
	
	private Object controllersLock = new Object();
	private HashMap<Integer, Controller> controllers = new HashMap<Integer, Controller>();
	
	private Thread networkThread;
	
	private TCPClient tcp;
	private UDPClient udp;
	
	private int packetInterval = 100;
	
	private enum NetworkState {
		DISCONNECTED,
		FRESH, //Connected, no handshake
		CONNECTED
	}
	private NetworkState networkState = NetworkState.DISCONNECTED;
	
	public NetworkManager() throws Exception {
		if (instance != null) {
			throw new Exception("NetworkManager already has an instance!");
		}
		instance = this;
		
		networkThread = new Thread(this);
		tcp = new TCPClient();
		udp = new UDPClient();
		
		pingThread.start();
		
		clientID = ThreadLocalRandom.current().nextInt();
		System.out.println("[NetworkManager] clientID: " + clientID);
	}
	
	private int runLoopCounter = 0;
	@Override
	public void run() {
		for (;;) {
			for (;;) {
				/*
				 * Socket is connected, handshake needed
				 */
				if (networkState == NetworkState.FRESH) {
					try {
						switch (tcp.doHandshake()) {
						
						case BAD_HANDSHAKE:
							tcp.abort();
							networkState = NetworkState.DISCONNECTED;
							continue;
							
						case NEW_CLIENT:
							synchronized (controllersLock) {
								for (Controller c : controllers.values()) {
									tcp.sendAttach(c);
								}
							}
							networkState = NetworkState.CONNECTED;
							break;
							
						case SAME_CLIENT:
							networkState = NetworkState.CONNECTED;
							break;
							
						default:
							tcp.abort();
							networkState = NetworkState.DISCONNECTED;
							continue;
							
						}		
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (networkState == NetworkState.CONNECTED) {
					synchronized (controllersLock) {
						try {
							udp.send(controllers);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					if (runLoopCounter++ == PING_INTERVAL / packetInterval) {
						synchronized(pingThread) {
							pingThread.notify();
						}
						runLoopCounter = 0;
					}
					
					sleep(packetInterval);
				} else if (networkState == NetworkState.DISCONNECTED) break;
			} //for (;;)
			
			try {
				synchronized (networkThread) {
					networkThread.wait();
				}
			} catch (InterruptedException e) {}
		}
	}
	
	public void connect(String ip) {
		System.out.println("[NetworkManager] Connecting to " + ip + "..."); //XXX debug text
		try {
			udp.connect(ip);
			tcp.connect(ip);
		} catch (Exception e) {
			System.err.println("[NetworkManager] Couldn't connect to Wii U!");
			e.printStackTrace();
			return;
		}
		networkState = NetworkState.FRESH;
		if (networkThread.getState() == Thread.State.NEW) {
			networkThread.start();
		} else if (networkThread.getState() == Thread.State.WAITING) {
			synchronized (networkThread) {
				networkThread.notify();
			}
		}
	}
	
	public void disconnect() {
		networkState = NetworkState.DISCONNECTED;
		
		if (!Thread.currentThread().equals(networkThread) && networkThread.getState() != Thread.State.NEW) {
			while (networkThread.getState() != Thread.State.WAITING) {sleep(1);}
		}
		
		try {
			tcp.abort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("[NetworkManager] Disconnected.");
	}
	
	/*
	 * Is there an active connection?
	 */
	public boolean isConnected() {
		return networkState == NetworkState.FRESH || networkState == NetworkState.CONNECTED;
	}
	
	/*
	 * Is the active connection good for data?
	 */
	public boolean isRunning() {
		return networkState == NetworkState.CONNECTED;
	}
	
	public HashMap<Integer,Controller> getControllers() {
		return controllers;
	}
	
	public void setPacketInterval(int packetInterval) {
		this.packetInterval = packetInterval;
	}
	
	public void addController(Controller controller) {
		synchronized (controllersLock) {
			if (isRunning()) {
				try {
					tcp.sendAttach(controller);
				} catch (Exception e) {return;};
			}
			controllers.put(controller.getID().hashCode(), controller);
		}
	}
	
	public void removeController(Controller controller) {
		synchronized (controllersLock) {
			if (isRunning()) {
				try {
					tcp.sendDetach(controller);
				} catch (Exception e) {return;};
			}
			controller.destroy();
			controllers.remove(controller.getID().hashCode());
		}
	}
	
	public void removeAllControllers() {
		synchronized (controllersLock) {
			for (Controller c : controllers.values()) {
				if (isRunning()) {
					try {
						tcp.sendDetach(c);
					} catch (Exception e) {continue;};
				}
				c.destroy();
				controllers.remove(c.getID().hashCode());
			}
		}
	}
	
	private Thread pingThread = new Thread(new Runnable() {
		public void run() {
			for (;;) {
				synchronized (pingThread) {
					try {
						pingThread.wait();
					} catch (InterruptedException e) {}
				}
				
				if (!tcp.ping()) {
					System.out.println("[NetworkManager] Ping failed, disconnecting...");
					GuiInteractionManager.instance().disconnect();
				}
			}
		}
	}, "Ping Thread");
	
	private void sleep(long ticks) {
		try {
			Thread.sleep(ticks);
		} catch (InterruptedException e) {}
	}
	
	public static NetworkManager instance() {
		return instance;
	}
}
