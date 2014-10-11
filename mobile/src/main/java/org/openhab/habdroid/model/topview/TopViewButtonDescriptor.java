package org.openhab.habdroid.model.topview;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewButtonDescriptor {
    private final int left;
    private final int top;
    private final int width;
    private final int height;
    private final String item;

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getItem() {
        return item;
    }

    private TopViewButtonDescriptor(int left, int top, int width, int height, String item) {
        assert left >= 0;
        assert top >= 0;
        assert width >= 0;
        assert height >= 0;
        assert !StringUtil.isStringUndefinedOrEmpty(item);

        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.item = item;
    }

    public static TopViewButtonDescriptor create(int left, int top, int width, int height, String item) {
        if (left < 0) {
            throw new IllegalArgumentException("left must be >= 0!");
        }
        if (top < 0) {
            throw new IllegalArgumentException("top must be >= 0!");
        }
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0!");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0!");
        }
        if (StringUtil.isStringUndefinedOrEmpty(item)) {
            throw new IllegalArgumentException("item must not be undefined or empty!");
        }

        return new TopViewButtonDescriptor(left, top, width, height, item);
    }
}