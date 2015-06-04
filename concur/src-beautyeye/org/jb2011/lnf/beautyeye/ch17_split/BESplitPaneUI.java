/*
 * Copyright (C) 2015 Jack Jiang(cngeeker.com) The BeautyEye Project. 
 * All rights reserved.
 * Project URL:https://github.com/JackJiang2011/beautyeye
 * Version 3.6
 * 
 * Jack Jiang PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * BESplitPaneUI.java at 2015-2-1 20:25:40, original version by Jack Jiang.
 * You can contact author with jb2011@163.com.
 */
package org.jb2011.lnf.beautyeye.ch17_split;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

// TODO: Auto-generated Javadoc
/**
 * 分栏面板的UI实现.
 * 
 * @author Jack Jiang(jb2011@163.com), 2012-07-10
 * @version 1.0
 */
public class BESplitPaneUI  extends BasicSplitPaneUI
{
    
    /**
     * Instantiates a new bE split pane ui.
     */
    public BESplitPaneUI() 
    {
    	super();
    }

    /**
     * Creates a new BESplitPaneUI instance.
     *
     * @param x the x
     * @return the component ui
     */
    public static ComponentUI createUI(JComponent x) 
    {
    	return new BESplitPaneUI();
    }

    /**
     * Creates the default divider.
     *
     * @return the basic split pane divider
     */
    public BasicSplitPaneDivider createDefaultDivider() 
    {
    	return new BESplitPaneDivider(this);
    }
}
