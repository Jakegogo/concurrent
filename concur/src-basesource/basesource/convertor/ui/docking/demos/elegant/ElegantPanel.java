package basesource.convertor.ui.docking.demos.elegant;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.tools.Utilities;
import basesource.convertor.ui.docking.Dockable;
import basesource.convertor.ui.extended.RoundedPanel;
import basesource.convertor.utils.SmoothUtilities;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;


public class ElegantPanel extends RoundedPanel {
	private static final Color GRAD_MID = new Color(168, 203, 239);
	private static final Color GRAD_START = new Color(168, 203, 239);
			
	private Dockable dockable;
	private JLabel titleLabel;
	
	public ElegantPanel(String title) {
		super.add(getTitleLabel());
		setTitle(title);
		dockable = getDockable();
		setOpaque(false);
	}
	
	public void doLayout() {
		Insets insets = getInsets();
		int w = getWidth()-insets.left-insets.right;
		int h = getHeight()-insets.top-insets.bottom;
		getTitleLabel().setBounds(insets.left+3, insets.top, w, 25);
	}
	
	public Dockable getDockable() {
		if(dockable==null)
			dockable = new DockableImpl(this, getTitleLabel());
		return dockable;
	}
	
	public String getTitle() {
		return getTitleLabel().getText();
	}

	private JLabel getTitleLabel() {
		if(titleLabel!=null)
			return titleLabel;
			
		titleLabel = new JLabel() {
			private static final long serialVersionUID = 5706007014158218952L;

			@Override
			protected void paintComponent(Graphics g) {
				SmoothUtilities.configureGraphics(g);
				super.paintComponent(g);
			}
		};
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
		return titleLabel;
	}
	
	public void paintComponent(Graphics g) {
		SmoothUtilities.configureGraphics(g);
		super.paintComponent(g);
		
		Insets in = getInsets();
//		int mid = getWidth()/2;
//		int farRight = getWidth()-in.right;
//		int w = farRight - in.left;
		
		int width = getWidth();
		int height = DefaultUIConstant.PANEL_TITTLE_BORDER_HEIGHT;
		int x = in.left;
		int y = in.top;
		
		BufferedImage titleImage = Utilities.createTranslucentImage(width, height);
        GradientPaint gradient = new GradientPaint(0.0F, 0.0F,
        		GRAD_START, 0.0F, getHeight(),
        		GRAD_MID, false);
        Graphics2D g2 = (Graphics2D) titleImage.getGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(Utilities.deriveColorHSB(
        		GRAD_MID, 0.0F, 0.0F, -0.2F));
        g2.drawLine(x + 1, height - 1, width - 2, height - 1);
        g2.setColor(Utilities.deriveColorHSB(
        		GRAD_MID, 0.0F, -0.5F, 0.5F));
        g2.drawLine(x + 1, height, width - 2, height);
        g2.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0.0F, 0.0F, 0.0F, 1.0F),
                width, 0.0F, new Color(0.0F, 0.0F, 0.0F, 0.0F)));
        g2.setComposite(AlphaComposite.DstIn);
        g2.fillRect(x, y, width, height);
        g2.dispose();

        g.drawImage(titleImage, in.left, in.top, this);
        
		
		

//		GradientPaint firstHalf = new GradientPaint(in.left, y, GRAD_START, mid, y, GRAD_MID);
//		GradientPaint secondHalf = new GradientPaint(mid, y, GRAD_MID, farRight, y, getBackground());
//
//		Graphics2D g2 = (Graphics2D)g;
//		
//		g2.setPaint(firstHalf);
//		g2.fillRoundRect(in.left, in.top, w/2, 25, 10, 25);
////		g2.fillRect(in.left, in.top, w/2, 25);
//		g2.setPaint(secondHalf);
//		g2.fillRect(mid-1, in.top, w/2, 25);
//
//		g.setColor(getBackground().brighter());
//		g.drawLine(in.left, in.top, farRight,  in.top);
//		g.drawLine(in.left, in.top, in.left, in.top+25);
//
//		g.setColor(getBackground().darker());
//		g.drawLine(in.left, in.top+25, farRight, in.top+25);
	}
	
	public void setLayout(LayoutManager mgr) {
		// do nothing.  we handle our own layout.
	}

	public void setTitle(String title) {
		if(title==null)
			title = "";
		title = title.trim();
		getTitleLabel().setText(title);
	}
}
