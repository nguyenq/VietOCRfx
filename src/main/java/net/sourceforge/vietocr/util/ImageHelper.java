/**
 * Copyright @ 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.vietocr.util;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Common image processing routines.
 */
public class ImageHelper {

    /**
     * Returns the supplied src image brightened by a float value from 0 to 10.
     * Float values below 1.0f actually darken the source image.
     *
     * @param src
     * @param offset
     * @return
     */
    public static BufferedImage brighten(BufferedImage src, float offset) {
        RescaleOp rop = new RescaleOp(1, offset, null);
        return rop.filter(src, null);
    }

    /**
     * Contrasts image.
     *
     * @param src
     * @param scaleFactor
     * @return
     */
    public static BufferedImage contrast(BufferedImage src, float scaleFactor) {
        RescaleOp rop = new RescaleOp(scaleFactor, 0, null);
        return rop.filter(src, null);
    }

    // allow a 10px-margin
    private static final int margin = 10;

    /**
     * Auto crops an image.
     *
     * @param source
     * @param tolerance range from 0.0 to 0.5
     * @return
     */
    public static BufferedImage autoCrop(BufferedImage source, double tolerance) {
        // Get top-left pixel color as the "baseline" for cropping
        int baseColor = source.getRGB(0, 0);

        int width = source.getWidth();
        int height = source.getHeight();

        int minX = 0;
        int minY = 0;
        int maxX = width;
        int maxY = height;

        // Immediately break the loops when encountering a non-white pixel.
        lable1:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
                    minY = y;
                    break lable1;
                }
            }
        }

        lable2:
        for (int x = 0; x < width; x++) {
            for (int y = minY; y < height; y++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
                    minX = x;
                    break lable2;
                }
            }
        }

        // Get lower-left pixel color as the "baseline" for cropping
        baseColor = source.getRGB(minX, height - 1);

        lable3:
        for (int y = height - 1; y >= minY; y--) {
            for (int x = minX; x < width; x++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
                    maxY = y;
                    break lable3;
                }
            }
        }

        lable4:
        for (int x = width - 1; x >= minX; x--) {
            for (int y = minY; y < maxY; y++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
                    maxX = x;
                    break lable4;
                }
            }
        }

        if ((minX - margin) >= 0) {
            minX -= margin;
        }

        if ((minY - margin) >= 0) {
            minY -= margin;
        }

        if ((maxX + margin) < width) {
            maxX += margin;
        }

        if ((maxY + margin) < height) {
            maxY += margin;
        }

        int newWidth = maxX - minX + 1;
        int newHeight = maxY - minY + 1;
        
        // if same size, return the original
        if (newWidth == width && newHeight == height) {
            return source;
        }

        BufferedImage target = new BufferedImage(newWidth, newHeight, source.getType());

        Graphics g = target.getGraphics();
        g.drawImage(source, 0, 0, target.getWidth(), target.getHeight(),
                minX, minY, maxX + 1, maxY + 1, null);

        g.dispose();

        return target;
    }

    /**
     * Determines color distance.
     * http://stackoverflow.com/questions/10678015/how-to-auto-crop-an-image-white-border-in-java
     *
     * @param a a RGB value
     * @param b a RGB value
     * @param tolerance
     * @return
     */
    private static boolean colorWithinTolerance(int a, int b, double tolerance) {
        int aAlpha = (int) ((a & 0xFF000000) >>> 24);   // Alpha level
        int aRed = (int) ((a & 0x00FF0000) >>> 16);   // Red level
        int aGreen = (int) ((a & 0x0000FF00) >>> 8);    // Green level
        int aBlue = (int) (a & 0x000000FF);            // Blue level

        int bAlpha = (int) ((b & 0xFF000000) >>> 24);   // Alpha level
        int bRed = (int) ((b & 0x00FF0000) >>> 16);   // Red level
        int bGreen = (int) ((b & 0x0000FF00) >>> 8);    // Green level
        int bBlue = (int) (b & 0x000000FF);            // Blue level

        double distance = Math.sqrt((aAlpha - bAlpha) * (aAlpha - bAlpha)
                + (aRed - bRed) * (aRed - bRed)
                + (aGreen - bGreen) * (aGreen - bGreen)
                + (aBlue - bBlue) * (aBlue - bBlue));

        // 510.0 is the maximum distance between two colors 
        // (0,0,0,0 -> 255,255,255,255)
        double percentAway = distance / 510.0d;

        return (percentAway > tolerance);
    }

    /**
     * Sharpens an image.
     *
     * @param image
     * @return
     */
    public static BufferedImage sharpen(BufferedImage image) {
        // A 3x3 kernel that sharpens an image
        Kernel kernel = new Kernel(3, 3,
                new float[]{
                    -1, -1, -1,
                    -1, 9, -1,
                    -1, -1, -1
                });

        BufferedImageOp op = new ConvolveOp(kernel);

        return op.filter(image, null);
    }

    /**
     * Smooths or blurs an image.
     *
     * @param image
     * @return
     */
    public static BufferedImage smoothen(BufferedImage image) {
        // A 3x3 kernel that smoothens an image
        float data1[] = {
            0.1111f, 0.1111f, 0.1111f,
            0.1111f, 0.1111f, 0.1111f,
            0.1111f, 0.1111f, 0.1111f
        };

        Kernel kernel = new Kernel(3, 3, data1);
        BufferedImageOp op = new ConvolveOp(kernel);

        return op.filter(image, null);
    }
    
    /**
     * https://github.com/redwarp/9-Patch-Resizer/blob/develop/src/net/redwarp/tool/resizer/worker/ImageScaler.java
     *
     * @param image
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static BufferedImage rescaleImage(BufferedImage image, int targetWidth, int targetHeight) {
        if (targetWidth == 0) {
            targetWidth = 1;
        }
        if (targetHeight == 0) {
            targetHeight = 1;
        }
        if (targetWidth * 2 < image.getWidth() - 1) {
            BufferedImage tempImage = rescaleImage(image, image.getWidth() / 2, image.getHeight() / 2);
            return rescaleImage(tempImage, targetWidth, targetHeight);
        } else {
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = outputImage.createGraphics();

            Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
            hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHints(hints);
            g2d.drawImage(image, 0, 0, outputImage.getWidth(), outputImage.getHeight(), null);
            g2d.dispose();

            return outputImage;
        }
    }
}
