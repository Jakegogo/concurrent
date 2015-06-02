package basesource.convertor.ui.extended;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import javax.swing.*;

/**
 * 含进场渐变动画的面板
 */
public class AnimatingSplitPane extends JSplitPane {
	private static final long serialVersionUID = 7237574876567801139L;

	private boolean firstExpanded = false;

    private int lastDividerLocation = -1;

    public AnimatingSplitPane(int orientation) {
        super(orientation);
        setOneTouchExpandable(false);
    }

    public void setExpanded(boolean expanded) {
        if (expanded != this.firstExpanded) {
            if (!this.firstExpanded) {
                this.lastDividerLocation = getDividerLocation();
            }

            this.firstExpanded = expanded;

            Animator animator = new Animator(500,
                    new PropertySetter(this, "dividerLocation",
                            new Integer[]{
                                    Integer.valueOf(getDividerLocation()),
                                    Integer.valueOf(expanded ? getHeight() : this.lastDividerLocation)
                            }));

            animator.setStartDelay(10);
            animator.setAcceleration(0.2F);
            animator.setDeceleration(0.3F);
            animator.start();
        }
    }

    public void setDividerLocation(int dividerLocation) {
        super.setDividerLocation(dividerLocation);
    }
}