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
package net.ash.HIDToVPADNetworkClient.hid;

//TODO: Documentation...
public interface HidDevice {
    /**
     * Opens the HidDevice for usage.
     * 
     * @return true on success, false when it failed.
     */
    boolean open();

    /**
     * Closes the HidDevice
     */
    void close();

    /**
     * Returns the VendorID of the HidDevice
     * 
     * @return vendorID
     */
    short getVendorId();

    /**
     * Returns the ProductID of the HidDevice
     * 
     * @return productID
     */
    short getProductId();

    /**
     * Returns the latest data.
     * 
     * @return An byte array containing the latest data. If no data is present, it'll return a empty byte array
     */
    byte[] getLatestData();

    /**
     * Retuns the Usage Page of this HID-Device
     * 
     * @return usage page
     */
    short getUsagePage();

    /**
     * Retuns the Usage ID of this HID-Device
     * 
     * @return usage id
     */
    short getUsageID();

    /**
     * Returns the path of this HidDevice
     * 
     * @return path
     */
    String getPath();

    /**
     * Returns the name of the HID device
     * 
     * @return product string (name)
     */
    String getProductString();
}
