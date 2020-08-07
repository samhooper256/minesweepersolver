package minesweeper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *	A subclass of {@link ImageView} that allows for reisizing. Taken from
 * <a href=https://stackoverflow.com/a/62440226/11788023> this answer</a> and modified slightly by me.
 */
class WrappedImageView extends ImageView {	
	private final int minWidth, minHeight;
	public WrappedImageView(Image im, int minWidth, int minHeight) {
		super(im);
        setPreserveRatio(false);
        this.minWidth = minWidth;
        this.minHeight = minHeight;
	}
	
    public WrappedImageView(Image im)
    {
    	this(im, 20, 20);
    }

    @Override
    public double minWidth(double height)
    {
        return minWidth;
    }

    @Override
    public double prefWidth(double height)
    {
        Image I=getImage();
        if (I==null) return minWidth;
        return I.getWidth();
    }

    @Override
    public double maxWidth(double height)
    {
        return 16384;
    }

    @Override
    public double minHeight(double width)
    {
        return minHeight;
    }

    @Override
    public double prefHeight(double width)
    {
        Image I=getImage();
        if (I==null) return minHeight;
        return I.getHeight();
    }

    @Override
    public double maxHeight(double width)
    {
        return 16384;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double width, double height)
    {
        setFitWidth(width);
        setFitHeight(height);
    }
}
