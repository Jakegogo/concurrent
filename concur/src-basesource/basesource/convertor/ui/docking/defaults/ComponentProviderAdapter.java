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
package basesource.convertor.ui.docking.defaults;

import basesource.convertor.ui.docking.DockingPort;

import javax.swing.*;

/**
 * Provides a default implementation of the <code>SubComponentProvider</code>.  This class may be extended
 * in any application that wishes to make use of the <code>SubComponentProvider</code> interface without
 * the need for writing out an implementation for every method that isn't explicitly used.
 * 
 * @author Chris Butler
 */
public class ComponentProviderAdapter implements SubComponentProvider {

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.defaults.SubComponentProvider#createChildPort()
	 */
	public DockingPort createChildPort() {
		return null;
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.defaults.SubComponentProvider#createSplitPane()
	 */
	public JSplitPane createSplitPane() {
		return null;
	}

	/**
	 * Returns null.
	 * @see basesource.convertor.ui.docking.defaults.SubComponentProvider#createTabbedPane()
	 */
	public JTabbedPane createTabbedPane() {
		return null;
	}

	/**
	 * Returns -1.
	 * @see basesource.convertor.ui.docking.defaults.SubComponentProvider#getInitialDividerLocation()
	 */
	public double getInitialDividerLocation() {
		return 0.3d;
	}

}
