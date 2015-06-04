/*
 * Copyright (C) 2015 Jack Jiang(cngeeker.com) The BeautyEye Project. 
 * All rights reserved.
 * Project URL:https://github.com/JackJiang2011/beautyeye
 * Version 3.6
 * 
 * Jack Jiang PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * BEProgressBarUI.java at 2015-2-1 20:25:39, original version by Jack Jiang.
 * You can contact author with jb2011@163.com.
 */
package org.jb2011.lnf.beautyeye.ch12_progress;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.jb2011.lnf.beautyeye.utils.ReflectHelper;
import org.jb2011.lnf.beautyeye.winlnfutils.WinUtils;
import org.jb2011.ninepatch4j.NinePatch;

/**
 * 进度条的UI实现类.
 * 
 * @author Jack Jiang(jb2011@163.com)
 */
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 一些说明 Start
//本类的实现参考了WindowsProgressBarUI，完全进行了重新实现（基本上没有WindowsProgressBarUI的代码了）
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 一些说明 END
public class BEProgressBarUI extends BasicProgressBarUI 
	implements org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.__UseParentPaintSurported
{
    
    /**
     * Creates the ui.
     *
     * @param x the x
     * @return the component ui
     */
    public static ComponentUI createUI(JComponent x) 
    {
    	return new BEProgressBarUI();
    }
    
    //* 本方法由Jack Jiang于2012-09-07日加入
    /**
     * 是否使用父类的绘制实现方法，true表示是.
     * <p>
     * 因为在BE LNF中，进度条和背景都是使用N9图，没法通过设置JProgressBar的背景色和前景
     * 色来控制进度条的颜色，本方法的目的就是当用户设置了进度条的Background或Foreground
     * 时告之本实现类不使用BE LNF中默认的N9图填充绘制而改用父类中的方法（父类中的方法
     * 就可以支持颜色的设置罗，只是丑点，但总归是能适应用户的需求场景要求，其实用户完全可以
     * 通过JProgressBar.setUI(new MetalProgressBar())方式来自定义进度的UI哦）.
     *
     * @return true, if is use parent paint
     */
    public boolean isUseParentPaint()
    {
    	return progressBar != null 
    		&& (!(progressBar.getForeground() instanceof UIResource)
				|| !(progressBar.getBackground() instanceof UIResource));
    }

    //* copy from BasicProgressBarUI and modified by Jack Jiang
    //绘制普通进度条的方法
	/**
     * All purpose paint method that should do the right thing for almost all
     * linear, determinate progress bars. By setting a few values in the
     * defaults table, things should work just fine to paint your progress bar.
     * Naturally, override this if you are making a circular or semi-circular
     * progress bar.
     *
     * @param g the g
     * @param c the c
     * @see #paintIndeterminate
     * @since 1.4
     */
	protected void paintDeterminate(Graphics g, JComponent c)
	{
		if (!(g instanceof Graphics2D))
		{
			return;
		}
		
		//* 如果用户作了自定义颜色设置则使用父类方法来实现绘制，否则BE LNF中没法支持这些设置哦
		if(isUseParentPaint())
		{
			super.paintDeterminate(g, c);
			return;
		}

		Insets b = progressBar.getInsets(); // area for border
		int barRectWidth = progressBar.getWidth() - (b.right + b.left);
		int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
		
		//* add by Jack Jiang 2012-06-20 START
		//绘制进度条的背景
		paintProgressBarBgImpl(progressBar.getOrientation() == JProgressBar.HORIZONTAL
				, g, b, barRectWidth, barRectHeight);
		//* add by Jack Jiang 2012-06-20 END
			
		if (barRectWidth <= 0 || barRectHeight <= 0)
		{
			return;
		}

		int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(progressBar.getForeground());//在BE LNF中本属性设置目前没有意义哦，因为用的都是n9图

		if (progressBar.getOrientation() == JProgressBar.HORIZONTAL)
		{
			if (WinUtils.isLeftToRight(c))
			{
				paintProgressBarContentImpl(true, g,b.left, b.top
						,amountFull, barRectHeight, -1);
			}
			// TODO 以下代码未经测试
			else
			{
				paintProgressBarContentImpl(true, g,barRectWidth+b.left, b.top
						, barRectWidth + b.left - amountFull, barRectHeight, -1);
			}
		}
		else
		{ // VERTICAL
			paintProgressBarContentImpl(false, g, b.left, b.top + barRectHeight - amountFull
					, barRectWidth, amountFull, barRectHeight);
		}

		// Deal with possible text painting
		if (progressBar.isStringPainted())
		{
			paintString(g, b.left, b.top, barRectWidth, barRectHeight,amountFull, b);
		}
	}
	
	//* copy from BasicProgressBarUI and modified by Jack Jiang
	//绘制无穷进度条的方法
	/**
	 * All purpose paint method that should do the right thing for all
	 * linear bouncing-box progress bars.
	 * Override this if you are making another kind of
	 * progress bar.
	 *
	 * @param g the g
	 * @param c the c
	 * @see #paintDeterminate
	 * @since 1.4
	 */
	protected void paintIndeterminate(Graphics g, JComponent c) 
	{
		if (!(g instanceof Graphics2D)) 
		{
			return;
		}
		
		//* 如果用户作了自定义颜色设置则使用父类方法来实现绘制，否则BE LNF中没法支持这些设置哦
		if(isUseParentPaint())
		{
			super.paintIndeterminate(g, c);
			return;
		}

		Insets b = progressBar.getInsets(); // area for border
		int barRectWidth = progressBar.getWidth() - (b.right + b.left);
		int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

		if (barRectWidth <= 0 || barRectHeight <= 0) {
			return;
		}
		
		//* add by Jack Jiang 2012-06-20 START
		//绘制进度条的背景
		paintProgressBarBgImpl(progressBar.getOrientation() == JProgressBar.HORIZONTAL, g,b,barRectWidth, barRectHeight);
		//* add by Jack Jiang 2012-06-20 END
		
		Graphics2D g2 = (Graphics2D)g;

		// Paint the bouncing box.
		boxRect = getBox(boxRect);
		if (boxRect != null) 
		{
			g2.setColor(progressBar.getForeground());//BE LNF中，目前本颜色设置无意义哦，因使用的都是N9图
			//由Jack Jiang修改
//			g2.fillRect(boxRect.x, boxRect.y, boxRect.width, boxRect.height);
			paintProgressBarContentImpl(progressBar.getOrientation() == JProgressBar.HORIZONTAL
					, g,boxRect.x, boxRect.y, boxRect.width, boxRect.height, boxRect.height);//水平时最后一个参数无意义哦
		}

		// Deal with possible text painting
		if (progressBar.isStringPainted()) 
		{
			if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) 
			{
				paintString(g2, b.left, b.top, barRectWidth, barRectHeight,boxRect.x, boxRect.width, b);
			}
			else 
			{
				paintString(g2, b.left, b.top, barRectWidth, barRectHeight,boxRect.y, boxRect.height, b);
			}
		}
	}
	
	//* add by Jack Jiang
	/**
	 * 进度条当前值的绘制实现方法.
	 *
	 * @param isHorizontal true表示水平进度条，否则表示垂直进度条
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @param amountFull the amount full
	 * @param barContentRectHeight the bar content rect height
	 * @param barSumHeightForVertival 本参数只在垂直进度条时有意义，目的是为了在当前值很
	 * 小的情况下为了达到N9图最小绘制高度时，作修正时需要
	 */
	protected void paintProgressBarContentImpl(boolean isHorizontal
			,Graphics g,int x,int y,int amountFull,int barContentRectHeight
			, int barSumHeightForVertival)
	{
		NinePatch np;
		
		//当前的进度条内容.9.png图片的边缘非填充部分是17像素，如果要
		//填充的总宽度小于此则会出现NinePatch填充算法无法解决的填充，
		//以下判断将在总宽度小于此值时强制设置成最小宽度
		final int n9min = 17;// TODO 14是相关于.9.png图片的最小填充宽度的，最好用常量实现
		if(isHorizontal)
		{
			//如果最小填充长度小于n9图的最小长度最设定为最小长度，否则N9的填充会很难看哦
			if(amountFull > 0 && amountFull < n9min)
			{
				amountFull = n9min;
			}
			np = __Icon9Factory__.getInstance().getProgressBar_green();
		}
		else
		{
			//如果最小填充长度小于n9图的最小长度最设定为最小长度，否则N9的填充会很难看哦
			if(barContentRectHeight > 0 && barContentRectHeight < n9min)
			{
				y = barSumHeightForVertival - n9min;
				barContentRectHeight = n9min;
			}
			np = __Icon9Factory__.getInstance().getProgressBar_blue_v();
		}
		//开始绘制N9图
		np.draw((Graphics2D)g, x, y, amountFull, barContentRectHeight);
	}
	
	//* add by Jack Jiang
	/**
	 * 进度条背景填充实现方法.
	 *
	 * @param isHorizontal the is horizontal
	 * @param g the g
	 * @param b the b
	 * @param barRectWidth the bar rect width
	 * @param barRectHeight the bar rect height
	 */
	protected void paintProgressBarBgImpl(boolean isHorizontal,Graphics g,Insets b,int barRectWidth,int barRectHeight)
	{
		NinePatch np;
		if(isHorizontal)
			np = __Icon9Factory__.getInstance().getProgressBarBg();
		else
			np = __Icon9Factory__.getInstance().getProgressBarBg_v();
		np.draw((Graphics2D)g, b.left, b.top, barRectWidth, barRectHeight);
	}
	
	//* 原方法在本子类中不可见，无法调用，所以只能复制过来罗
	/**
	 * Paints the progress string.
	 *
	 * @param g Graphics used for drawing.
	 * @param x x location of bounding box
	 * @param y y location of bounding box
	 * @param width width of bounding box
	 * @param height height of bounding box
	 * @param fillStart start location, in x or y depending on orientation,
	 *        of the filled portion of the progress bar.
	 * @param amountFull size of the fill region, either width or height
	 *        depending upon orientation.
	 * @param b Insets of the progress bar.
	 */
	private void paintString(Graphics g, int x, int y, int width, int height,
			int fillStart, int amountFull, Insets b) 
	{
		//* 由Jack Jiang修改：因父类中的本方法是private（其实完全可以弄成protected），而如果要全部把该方法拷贝过来
		//* 则会因它调用了sun的非公开api而使得在不同的java版本里有兼容性问题（就是SwingUtilities2类，在1.5里
		//* 位于com.sun.swing.**而在1.6及1.7里是放在sun.swing里的，而且以后果的版本它还会不会改位置也说不定），
		//* 所以为了兼容性，此处利用反射来非正常的调用父类私有方法paintString(正常情况下子类是调用不到了父类的私有方法的撒)
		ReflectHelper.invokeMethod(BasicProgressBarUI.class, this, "paintString"
				, new Class[]{Graphics.class, int.class, int.class, int.class, int.class, int.class, int.class, Insets.class}
				, new Object[]{g,x,y,width,height,fillStart,amountFull,b});
	}
}

