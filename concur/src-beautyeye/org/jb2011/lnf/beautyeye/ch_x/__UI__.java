/*
 * Copyright (C) 2015 Jack Jiang(cngeeker.com) The BeautyEye Project. 
 * All rights reserved.
 * Project URL:https://github.com/JackJiang2011/beautyeye
 * Version 3.6
 * 
 * Jack Jiang PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * __UI__.java at 2015-2-1 20:25:36, original version by Jack Jiang.
 * You can contact author with jb2011@163.com.
 */
package org.jb2011.lnf.beautyeye.ch_x;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jb2011.lnf.beautyeye.widget.border.BEDashedRoundRecBorder;

/**
 * 各种未归类的UI属性设置实现类.
 * 本类中的各种属性日后可能会移入相应的各独立组件的UI包.
 * 
 * @author Jack Jiang
 * @version 1.1
 */
public class __UI__
{
	/**
	 * Ui impl.
	 */
	public static void uiImpl()
	{
		UIManager.put("control",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));
		UIManager.put("Separator.foreground",new ColorUIResource(new Color(180,180,180)));
		UIManager.put("ToolTip.foreground",new ColorUIResource(BeautyEyeLNFHelper.commonForegroundColor));
		
		UIManager.put("Separator.background",new ColorUIResource(Color.white));
		UIManager.put("Panel.foreground",new ColorUIResource(BeautyEyeLNFHelper.commonForegroundColor));
		UIManager.put("Panel.background",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));
		
		UIManager.put("Label.foreground",new ColorUIResource(BeautyEyeLNFHelper.commonForegroundColor));
		UIManager.put("Label.background",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));

		UIManager.put("ColorChooser.foreground",new ColorUIResource(BeautyEyeLNFHelper.commonForegroundColor));
		UIManager.put("ColorChooser.background",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));
		UIManager.put("ColorChooser.swatchesDefaultRecentColor",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));
		
		UIManager.put("TitledBorder.titleColor",new ColorUIResource(new Color(58,135,173)));//TitleBorder的标题颜色
		//TitledBorder的默认border实现（windows LNF中默认是圆色灰色实线距形）
		UIManager.put("TitledBorder.border",new BorderUIResource(new BEDashedRoundRecBorder(BeautyEyeLNFHelper.commonFocusedBorderColor)));
		
//		UIManager.put("OptionPaneUI",org.jb2011.lnf.windows2.ch3.NLOptionPaneUI.class.getName());
		//** Ui里的实现逻辑：此属性为true时将导致JOptionPane里的各按钮按BasicOptionPaneUI里设定的Insets进行
		//** UI展现：当按钮数<=2时使用的Insets=new Instes(2,8,2,8)，否则使用new Instes(2,4,2,4)，
		//** 这样的逻辑下，BeautyEye L&F实现里会使得按钮高度缩小而不好看，所以要关闭此属性
		UIManager.put("OptionPane.setButtonMargin",false);
		UIManager.put("OptionPane.foreground",new ColorUIResource(BeautyEyeLNFHelper.commonForegroundColor));
		UIManager.put("OptionPane.background",new ColorUIResource(BeautyEyeLNFHelper.commonBackgroundColor));
		UIManager.put("OptionPane.questionIcon",__IconFactory__.getInstance().getOptionPaneQUESTIONIcon());
		UIManager.put("OptionPane.warningIcon",__IconFactory__.getInstance().getOptionPaneWARNIcon());
		UIManager.put("OptionPane.informationIcon",__IconFactory__.getInstance().getOptionPaneINFOIcon());
		UIManager.put("OptionPane.errorIcon",__IconFactory__.getInstance().getOptionPaneERRORIcon());
		
		UIManager.put("SeparatorUI",org.jb2011.lnf.beautyeye.ch_x.BESeparatorUI.class.getName());
	}
}
