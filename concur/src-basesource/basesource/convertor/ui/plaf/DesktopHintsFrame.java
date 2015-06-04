package basesource.convertor.ui.plaf;

import javax.swing.*;

import basesource.convertor.utils.SmoothUtilities;

import java.awt.*;
import java.util.Map;

public class DesktopHintsFrame extends JFrame
{

	public static void main(String[] args)
	{

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new DesktopHintsFrame();
			}
		});
	}

	public DesktopHintsFrame()
	{
		JLabel lbl = new JLabel("文字效果测试")
		{
			protected void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g.create();
				SmoothUtilities.configureGraphics(g2);
				// 桌面文字属性必须是添加后才会出效，
				// 如果直接设置的话将不会产生效果
				// g2.setRenderingHints(desktopHints);
				super.paintComponent(g2);
				g2.dispose();
			}
		};
		lbl.setPreferredSize(new Dimension(300, 200));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(lbl);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
