package enterprises.iwakura.docs.util;

import com.hypixel.hytale.math.vector.Vector2d;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResizeUtils {

    /**
     * Resizes the specified Vector2d based on the alternative values. If both are non-zero, the new
     * size is exactly the specified values. If both are zero, the original is returned. Both values
     * must be valid numbers, otherwise the original is returned.
     *
     * @param original          Original size
     * @param alternativeWidth  Alternative value or zero to resize with respect to aspect ratio.
     * @param alternativeHeight Alternative value or zero to resize with respect to aspect ratio.
     *
     * @return New (or original) vector 2d
     */
    public Vector2d resize(Vector2d original, String alternativeWidth, String alternativeHeight) {
        int width, height;

        try {
            width = Integer.parseInt(alternativeWidth);
            height = Integer.parseInt(alternativeHeight);
        } catch (NumberFormatException ignored) {
            return original;
        }

        if (width >= 1 && height >= 1) {
            return new Vector2d(width, height);
        } else if (width == 0 && height == 0) {
            return original;
        } else {
            double aspectRatio = original.getX() / original.getY();

            if (width == 0) {
                // Scale width based on height
                return new Vector2d(height * aspectRatio, height);
            } else {
                // Scale height based on width
                return new Vector2d(width, width / aspectRatio);
            }
        }
    }
}
