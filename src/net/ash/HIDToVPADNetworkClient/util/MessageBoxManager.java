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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MessageBoxManager implements Runnable {
    private static final Queue<MessageBox> messageBoxQueue = new ConcurrentLinkedQueue<MessageBox>();
    private static List<MessageBoxListener> newList = Collections.synchronizedList(new ArrayList<MessageBoxListener>());
    private static Object listenerListLock = new Object();
    private static boolean threadStarted = false;

    private final static MessageBoxManager instance = new MessageBoxManager();

    private MessageBoxManager() {

    }

    @Override
    public void run() {
        while (true) {
            MessageBox msg = getNextMessage();
            if (msg != null) {
                synchronized (listenerListLock) {
                    for (MessageBoxListener m : newList) {
                        m.showMessageBox(msg);
                    }
                }
            }
            Utilities.sleep(500);
        }
    }

    public static void addMessageBox(String message) {
        addMessageBox(new MessageBox(message));
    }

    public static void addMessageBox(String message, int type) {
        addMessageBox(new MessageBox(message, type));
    }

    public static void addMessageBox(MessageBox messagebox) {
        messageBoxQueue.add(messagebox);
    }

    private static MessageBox getNextMessage() {
        return messageBoxQueue.poll();
    }

    public static void addMessageBoxListener(MessageBoxListener msglistener) {
        if (!threadStarted) {
            new Thread(instance, "MessageBoxManager").start();
            threadStarted = true;
        }
        newList.add(msglistener);
    }
}
