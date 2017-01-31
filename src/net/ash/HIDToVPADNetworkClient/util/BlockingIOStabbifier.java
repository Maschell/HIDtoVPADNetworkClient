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
package net.ash.HIDToVPADNetworkClient.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BlockingIOStabbifier extends InputStream implements Runnable {
	private byte[] buffer;
	private BufferedInputStream file;
	private ByteArrayOutputStream baos;
	private boolean running;
	
	int baosSize = 0;
	int bufferRemaining = 0;
	int bufferOffset = -1;
	
	public void run() {
		while (running) {
			try {
				synchronized (baos) {
					baos.write(file.read());
					baosSize++;
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void allocate() {
		synchronized (baos) {
			buffer = baos.toByteArray();
			bufferRemaining = baos.size();
			bufferOffset = -1;
			baos.reset();
			baosSize = 0;
		}
	}
	
	@Override
	public int read() throws IOException {
		if (bufferRemaining == 0) {
			allocate();
			if (bufferRemaining == 0) {
				return -1;
			}
		}
		bufferRemaining--;
		bufferOffset++;
		return buffer[bufferOffset] & 0xFF;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public void close() throws IOException {
		running = false;
		file.close();
		baos.close();
		super.close();
	}
	
	@Override
	public int available() {
		return bufferRemaining + baosSize;
	}
	
	public BlockingIOStabbifier(String path) throws FileNotFoundException {
		file = new BufferedInputStream(new FileInputStream(path));
		
		baos = new ByteArrayOutputStream();
		buffer = new byte[0];
		
		running = true;
		new Thread(this).start();
	}
}
