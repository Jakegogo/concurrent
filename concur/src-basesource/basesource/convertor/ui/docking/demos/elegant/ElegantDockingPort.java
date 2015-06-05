package basesource.convertor.ui.docking.demos.elegant;

import basesource.convertor.ui.docking.defaults.DefaultDockingPort;
import basesource.convertor.ui.docking.defaults.StandardBorderManager;


public class ElegantDockingPort extends DefaultDockingPort {
	public ElegantDockingPort() {
		setComponentProvider(new ChildComponentDelegate());
		setBorderManager(new StandardBorderManager(new ShadowBorder()));
	}
	
	public void add(ElegantPanel view) {
		dock(view.getDockable(), CENTER_REGION); 
	}
}
