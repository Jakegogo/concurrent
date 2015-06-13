package basesource.convertor.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Swing组件工具类
 */
public class SwingComponentUtils {

	/**
	 * 增加边距
	 * @param parent 父容器
	 * @param component 目标容器
	 * @param marginX x
	 * @param marginY y
	 */
	public static void addMargin(JComponent parent, JComponent component,
			int marginX, int marginY) {
		Insets insets = parent.getInsets();
		Rectangle bounds = component.getBounds();
		int w = bounds.width - 2 * marginX;
		int h = bounds.height - 2 * marginY;
		component.setBounds(insets.left + marginX, insets.top + marginY, w, h);
	}

}
