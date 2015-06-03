package basesource.convertor.ui.plaf;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DesktopHintsFrame extends JFrame
{
	private Map desktopHints = null;



	public static void main(String[] args)
	{
		// 以前代码用于获取系统桌面的文字提示
		Toolkit tk = Toolkit.getDefaultToolkit();
		final Map desktopHints = (Map) tk.getDesktopProperty("awt.font.desktophints");

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new DesktopHintsFrame(desktopHints);
			}
		});
	}

	public DesktopHintsFrame(final Map desktopHints)
	{
		JLabel lbl = new JLabel("文字效果测试")
		{
			protected void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g.create();
//				g2.setFont(new Font("微软雅黑", Font.PLAIN, 18));
				if (desktopHints != null)
					g2.addRenderingHints(desktopHints);
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
