package basesource.convertor.utils;

import java.awt.*;
import java.util.Map;

import basesource.convertor.contansts.DefaultUIConstant;

/**
 * A repository of utility code.
 *
 * @author James Shiell
 * @version 1.1
 * @since 0.3
 */
public class SmoothUtilities {

	static Toolkit tk = Toolkit.getDefaultToolkit();
	static final Map desktopHints = (Map) tk
			.getDesktopProperty("awt.font.desktophints");
	
	public static void configureGraphics(Graphics g) {
		// for antialiasing text
		((Graphics2D) g).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// for antialising geometric shapes
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		((Graphics2D) g).setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// to go for quality over speed
		((Graphics2D) g).setRenderingHint( RenderingHints.KEY_RENDERING,
	                            RenderingHints.VALUE_RENDER_QUALITY );
		
		((Graphics2D) g).setRenderingHints(desktopHints);
		((Graphics2D) g).setFont(DefaultUIConstant.DEFAULT_FONT);
	}

}