package basesource.convertor.ui.docking.demos.border;

import basesource.convertor.ui.docking.Dockable;
import basesource.convertor.ui.docking.DockableAdapter;
import basesource.convertor.ui.docking.DockingManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DockablePanel extends JPanel {
	private String title;
	private JPanel dragInit;
	private Dockable dockableImpl;

	public DockablePanel(String title) {
		super(new BorderLayout());
		dragInit = new JPanel();
		dragInit.setBackground(getBackground().darker());
		dragInit.setPreferredSize(new Dimension(10, 10));
		add(dragInit, BorderLayout.EAST);
		setBorder(new TitledBorder(title));
		setTitle(title);
		dockableImpl = new DockableImpl();
		DockingManager.registerDockable(dockableImpl);
	}

	private void setTitle(String title) {
		this.title = title;
	}

	Dockable getDockable() {
		return dockableImpl;
	}
	
	private DockablePanel getThis() {
		// used to return a reference to 'this' by inner classes.
		return this;
	}

	private class DockableImpl extends DockableAdapter {
		public Component getDockable() {
			return getThis();
		}
		public String getDockableDesc() {
			return title;
		}
		public Component getInitiator() {
			return dragInit;
		}
		public void setDockableDesc(String desc) {
			setTitle(desc);
		}
	}
}
