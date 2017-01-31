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
package net.ash.HIDToVPADNetworkClient.gui;

public class GuiController {
	private boolean activeState = false;
	private GuiControllerType type;
	private Object id;
	
	public GuiController(GuiControllerType type, Object id) {
		this.type = type;
		this.id = id;
	}
	
	public Object getId() {
		return id;
	}
	
	public GuiControllerType getType() {
		return type;
	}
	
	public boolean getActiveState() {
		return activeState;
	}
	
	public void setActiveState(boolean activeState) {
		this.activeState = activeState;
	}
	
	@Override
	public String toString() {
		return "GuiController, active: " + activeState + ", type: " + type.toString() + ", id: " + id.toString();
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() + type.hashCode() /*+ (activeState ? 1 : 0)*/;
	}
}
