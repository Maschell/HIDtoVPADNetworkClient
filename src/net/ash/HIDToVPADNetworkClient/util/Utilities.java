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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Utilities {

    private Utilities() {
    }

    /**
     * Let me just sleep!
     * 
     * @param ms
     *            sleep duration in ms
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Convert a byte array to a formated String
     * 
     * @param ba
     *            byte array
     * @return String representing the binary data
     */
    public static String ByteArrayToString(byte[] ba) {
        if (ba == null) return null;
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * Converts a signed short value to a unsigned byte
     * 
     * @param value
     *            short value
     * @return converted value
     */
    public static short signedShortToByte(int value) {
        return (short) (((((short) value) + Short.MAX_VALUE + 1) >> 8) & 0xFF);
    }

    /**
     * Converts a signed short value to a unsigned byte
     * 
     * @param value
     *            short value
     * @return converted value
     */
    public static short signedShortToByte(short value) {
        return signedShortToByte((int) value);
    }
    
    /**
     * Arrays.toString(boolean[]) in reverse.
     * https://stackoverflow.com/questions/456367/
     * @param string
     * @return array
     */
    public static boolean[] stringToBoolArray(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        boolean result[] = new boolean[strings.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = Boolean.parseBoolean(strings[i]);
        }
        return result;
    }
    
    public static String getStringFromInputStream(InputStream is) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        
        return baos.toString("UTF-8");
    }
}
