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
package net.ash.HIDToVPADNetworkClient.controller;

/**
 * Main controller interface, extended by controller drivers.
 * <br><br>
 * See {@link LinuxDevInputController} for a full implementation.
 * @author ash
 */
public interface Controller {
	/**
	 * Sets up the driver.
	 * <br>
	 * During this method call, a connection will be made with the controller hardware (if required).
	 * 
	 * @param arg Driver-specific init argument, see {@link ControllerManager} and {@link ControllerDetector}.
	 * @return Whether initialization was successful.
	 */
	public boolean initController(Object arg);
	
	/**
	 * Allocates and returns a copy of the latest data available from the controller.
	 * 
	 * @return A ControllerData instance containing the latest controller data.
	 */
	public ControllerData getLatestData();
	
	/**
	 * Used to tell a driver whether or not to poll the controller.
	 * <br>
	 * Is currently only ever used to initialize a driver (poll=true).
	 * destroy() is called for deinitialization.
	 * <br><br>
	 * <i>Candidate to be removed during refactoring.</i>
	 * 
	 * @param poll Whether or not the driver should poll the controller.
	 */
	public void setPollingState(boolean poll);
	
	/**
	 * Destroys the controller driver.
	 * <br>
	 * Will not return until all threads are stopped and resources are freed.
	 */
	public void destroy();
	
	/**
	 * Sets the deviceSlot and padSlot to be returned by {@link #getDeviceSlot() getDeviceSlot} and {@link #getPadSlot() getPadSlot}.
	 * @param deviceSlot Value to be returned by {@link #getDeviceSlot() getDeviceSlot}
	 * @param padSlot Value to be returned by {@link #getPadSlot() getPadSlot}
	 */
	public void setSlotData(short deviceSlot, byte padSlot);
	
	/**
	 * Gets the previously set device slot (see {@link #setSlotData(short, byte) setSlotData})
	 * @return The controller's device slot.
	 */
	public short getDeviceSlot();
	
	/**
	 * Gets the previously set pad slot (see {@link #setSlotData(short, byte) setSlotData})
	 * @return The controller's pad slot.
	 */
	public byte getPadSlot();
	
	/**
	 * Returns a unique handle for this controller driver.
	 * <br>
	 * Please note that this is unique to the <i>driver</i>, not the controller it's connected to.
	 * @return The driver's handle.
	 */
	public int getHandle();
	
	/**
	 * Gets the controller's ID. This is often identical to the argument to {@link #initController(Object) initController}.
	 * @return The controller's ID.
	 */
	public String getID();
}
