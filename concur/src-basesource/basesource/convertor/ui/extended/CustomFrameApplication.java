//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basesource.convertor.ui.extended;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CustomFrameApplication extends Application {
    private static final Logger logger = Logger.getLogger(CustomFrameApplication.class.getName());
    private ResourceMap appResources = null;
    private FrameView mainView = null;

    public CustomFrameApplication() {
    }

    public final JFrame getMainFrame() {
        return this.getMainView().getFrame();
    }

    protected final void setMainFrame(JFrame mainFrame) {
        this.getMainView().setFrame(mainFrame);
    }

    private String sessionFilename(Window window) {
        if(window == null) {
            return null;
        } else {
            String name = window.getName();
            return name == null?null:name + ".session.xml";
        }
    }

    protected void configureWindow(Window root) {
        this.getContext().getResourceMap().injectComponents(root);
    }

    private void initRootPaneContainer(RootPaneContainer c) {
        JRootPane rootPane = c.getRootPane();
        String k = "SingleFrameApplication.initRootPaneContainer";
        if(rootPane.getClientProperty(k) == null) {
            rootPane.putClientProperty(k, Boolean.TRUE);
            Container root = rootPane.getParent();
            if(root instanceof Window) {
                this.configureWindow((Window)root);
            }

            JFrame mainFrame = this.getMainFrame();
            Window filename;
            if(c == mainFrame) {
                mainFrame.addWindowListener(new CustomFrameApplication.MainFrameListener());
                mainFrame.setDefaultCloseOperation(0);
            } else if(root instanceof Window) {
                filename = (Window)root;
                filename.addHierarchyListener(new CustomFrameApplication.SecondaryWindowListener());
            }

            if(root instanceof JFrame) {
                root.addComponentListener(new CustomFrameApplication.FrameBoundsListener());
            }

            if(root instanceof Window) {
                filename = (Window)root;
                if(!root.isValid() || root.getWidth() == 0 || root.getHeight() == 0) {
                    filename.pack();
                }

                if(!filename.isLocationByPlatform() && root.getX() == 0 && root.getY() == 0) {
                    Object e = filename.getOwner();
                    if(e == null) {
                        e = filename != mainFrame?mainFrame:null;
                    }

                    filename.setLocationRelativeTo((Component)e);
                }
            }

            if(root instanceof Window) {
                String filename1 = this.sessionFilename((Window)root);
                if(filename1 != null) {
                    try {
                        System.out.println("session restoration temporarily disabled");
                    } catch (Exception var9) {
                        String msg = String.format("couldn\'t restore sesssion [%s]", new Object[]{filename1});
                        logger.log(Level.WARNING, msg, var9);
                    }
                }
            }

        }
    }

    protected void show(JComponent c) {
        if(c == null) {
            throw new IllegalArgumentException("null JComponent");
        } else {
            JFrame f = this.getMainFrame();
            f.getContentPane().add(c, "Center");
            this.initRootPaneContainer(f);

            this.postInit();

            f.setVisible(true);
        }
    }

    public void show(JDialog c) {
        if(c == null) {
            throw new IllegalArgumentException("null JDialog");
        } else {
            this.initRootPaneContainer(c);

            this.postInit();

            c.setVisible(true);
        }
    }

    public void show(JFrame c) {
        if(c == null) {
            throw new IllegalArgumentException("null JFrame");
        } else {
            this.initRootPaneContainer(c);

            this.postInit();

            c.setVisible(true);
        }
    }

    private void saveSession(Window window) {
        String filename = this.sessionFilename(window);
        if(filename != null) {
            try {
                this.getContext().getSessionStorage().save(window, filename);
            } catch (IOException var4) {
                logger.log(Level.WARNING, "couldn\'t save sesssion", var4);
            }
        }

    }

    private boolean isVisibleWindow(Window w) {
        return w.isVisible() && (w instanceof JFrame || w instanceof JDialog || w instanceof JWindow);
    }

    private List<Window> getVisibleSecondaryWindows() {
        ArrayList rv = new ArrayList();
        Method getWindowsM = null;

        try {
            getWindowsM = Window.class.getMethod("getWindows", new Class[0]);
        } catch (Exception var9) {
        }

        int len$;
        int i$;
        if(getWindowsM != null) {
            Window[] frames;

            try {
                frames = (Window[])((Window[])getWindowsM.invoke((Object)null, new Object[0]));
            } catch (Exception var8) {
                throw new Error("HCTB - can\'t get top level windows list", var8);
            }

            if(frames != null) {
                Window[] arr$ = frames;
                len$ = frames.length;

                for(i$ = 0; i$ < len$; ++i$) {
                    Window frame = arr$[i$];
                    if(this.isVisibleWindow(frame)) {
                        rv.add(frame);
                    }
                }
            }
        } else {
            Frame[] var10 = Frame.getFrames();
            if(var10 != null) {
                Frame[] var11 = var10;
                len$ = var10.length;

                for(i$ = 0; i$ < len$; ++i$) {
                    Frame var12 = var11[i$];
                    if(this.isVisibleWindow(var12)) {
                        rv.add(var12);
                    }
                }
            }
        }

        return rv;
    }

    protected void shutdown() {
        this.saveSession(this.getMainFrame());
        Iterator i$ = this.getVisibleSecondaryWindows().iterator();

        while(i$.hasNext()) {
            Window window = (Window)i$.next();
            this.saveSession(window);
        }

    }

    public FrameView getMainView() {
        if(this.mainView == null) {
            this.mainView = new FrameView(this);
        }

        return this.mainView;
    }

    public void show(View view) {
        if(this.mainView == null && view instanceof FrameView) {
            this.mainView = (FrameView)view;
        }

        RootPaneContainer c = (RootPaneContainer)view.getRootPane().getParent();
        this.initRootPaneContainer(c);

        this.postInit();

        ((Window)c).setVisible(true);
    }

    protected void postInit(){}

    private static class FrameBoundsListener implements ComponentListener {
        private FrameBoundsListener() {
        }

        private void maybeSaveFrameSize(ComponentEvent e) {
            if(e.getComponent() instanceof JFrame) {
                JFrame f = (JFrame)e.getComponent();
                if((f.getExtendedState() & 6) == 0) {
                    String clientPropertyKey = "WindowState.normalBounds";
                    f.getRootPane().putClientProperty(clientPropertyKey, f.getBounds());
                }
            }

        }

        public void componentResized(ComponentEvent e) {
            this.maybeSaveFrameSize(e);
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }
    }

    private class SecondaryWindowListener implements HierarchyListener {
        private SecondaryWindowListener() {
        }

        public void hierarchyChanged(HierarchyEvent e) {
            if((e.getChangeFlags() & 4L) != 0L && e.getSource() instanceof Window) {
                Window secondaryWindow = (Window)e.getSource();
                if(!secondaryWindow.isShowing()) {
                    CustomFrameApplication.this.saveSession(secondaryWindow);
                }
            }

        }
    }

    private class MainFrameListener extends WindowAdapter {
        private MainFrameListener() {
        }

        public void windowClosing(WindowEvent e) {
            CustomFrameApplication.this.exit(e);
        }
    }
}
