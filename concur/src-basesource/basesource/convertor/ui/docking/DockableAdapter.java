/* Copyright (c) 2004 Christopher M Butler

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal in the 
Software without restriction, including without limitation the rights to use, 
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
Software, and to permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package basesource.convertor.ui.docking;

import java.awt.Component;

/**
 * Provides a default implementation of the <code>Dockable</code> interface.  This class may be extended
 * in any application that wishes to make use of the <code>Dockable</code> interface without
 * the need for writing out an implementation for every method that isn't explicitly used.
 * 
 * @author Chris Butler
 */
public class DockableAdapter implements Dockable {
	
	/**
	 * Does nothing.
	 * @see basesource.convertor.ui.docking.Dockable#dockingCanceled()
	 */
	public void dockingCanceled() {
	}

	/**
	 * Does nothing.
	 * @see basesource.convertor.ui.docking.Dockable#dockingCompleted()
	 */
	public void dockingCompleted() {
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.Dockable#getCursorProvider()
	 */
	public CursorProvider getCursorProvider() {
		return null;
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.Dockable#getDockable()
	 */
	public Component getDockable() {
		return null;
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.Dockable#getDockableDesc()
	 */
	public String getDockableDesc() {
		return null;
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.Dockable#getInitiator()
	 */
	public Component getInitiator() {
		return null;
	}

	/**
	 * Returns true.
	 * @see basesource.convertor.ui.docking.Dockable#isDockedLayoutResizable()
	 */
	public boolean isDockedLayoutResizable() {
		return true;
	}

	/**
	 * Returns true.
	 * @see basesource.convertor.ui.docking.Dockable#isDockingEnabled()
	 */
	public boolean isDockingEnabled() {
		return true;
	}

	/**
	 * Returns true.
	 * @see basesource.convertor.ui.docking.Dockable#mouseMotionListenersBlockedWhileDragging()
	 */
	public boolean mouseMotionListenersBlockedWhileDragging() {
		return true;
	}

	/**
	 * Does nothing.
	 * @see basesource.convertor.ui.docking.Dockable#setDockableDesc(java.lang.String)
	 */
	public void setDockableDesc(String desc) {
	}

	/**
	 * Does nothing.
	 * @see basesource.convertor.ui.docking.Dockable#setDockedLayoutResizable(boolean)
	 */
	public void setDockedLayoutResizable(boolean b) {
	}

	/**
	 * Does nothing.
	 * @see basesource.convertor.ui.docking.Dockable#setDockingEnabled(boolean)
	 */
	public void setDockingEnabled(boolean b) {
	}

}
