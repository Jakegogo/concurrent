package basesource.convertor.ui.docking.demos.elegant;

import basesource.convertor.ui.docking.DockingPort;
import basesource.convertor.ui.docking.defaults.SubComponentProvider;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class ChildComponentDelegate implements SubComponentProvider {

	public DockingPort createChildPort() {
		ElegantDockingPort port = new ElegantDockingPort();
		port.setComponentProvider(this);
		return port;
	}

	public JSplitPane createSplitPane() {
		JSplitPane split = new JSplitPane();
		// remove the border from the split pane
		split.setBorder(null);

		// set the divider size for a more reasonable, less bulky look 
		split.setDividerSize(3);
		split.setOpaque(false);

		// check the UI.  If we can't work with the UI any further, then
		// exit here.
		if (!(split.getUI() instanceof BasicSplitPaneUI))
			return split;

		//  grab the divider from the UI and remove the border from it
		BasicSplitPaneDivider divider = 
				((BasicSplitPaneUI) split.getUI()).getDivider();
		if (divider != null)
			divider.setBorder(null);

		return split;
	}

	public JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setOpaque(false);
		return tabbedPane;
	}

	public double getInitialDividerLocation() {
		return 0.5d;
	}

}
