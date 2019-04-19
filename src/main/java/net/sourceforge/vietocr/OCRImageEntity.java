/**
 * Copyright @ 2009 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.vietocr;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.util.ImageHelper;

public class OCRImageEntity {

    /**
     * input images
     */
    private List<IIOImage> oimages;
    /**
     * input image File
     */
    private File imageFile;
    /**
     * input filename
     */
    private String inputfilename;

    /**
     * bounding rectangle
     */
    private Rectangle rect;
    
    /**
     * double-sided page
     */
    private boolean doublesided;

    /**
     * Horizontal Resolution
     */
    private int dpiX;
    /**
     * Vertical Resolution
     */
    private int dpiY;

    /**
     * Language code, which follows ISO 639-3 standard
     */
    StringProperty language = new SimpleStringProperty("eng");

    StringProperty languageProperty() {
        return language;
    }

    /**
     * index of pages, such as in multi-page TIFF image
     */
    IntegerProperty index = new SimpleIntegerProperty();
    
    IntegerProperty indexProperty() {
        return index;
    }

    /**
     * Constructor.
     *
     * @param oimages a list of <code>IIOImage</code> objects
     * @param inputfilename input filename
     * @param index index of images
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param doublesided single- or double-sided page
     * @param lang language code, which follows ISO 639-3 standard
     */
    public OCRImageEntity(List<IIOImage> oimages, String inputfilename, int index, Rectangle rect, boolean doublesided, String lang) {
        this.oimages = oimages;
        this.inputfilename = inputfilename;
        this.index.set(index);
        this.rect = rect;
        this.doublesided = doublesided;
        this.language.set(lang);
    }

    public OCRImageEntity(ArrayList<BufferedImage> images, String inputfilename, int index, Rectangle rect, boolean doublesided, String lang) {
        this(convertBufferedImageToIIOImage(images), inputfilename, index, rect, doublesided, lang);
    }

    static List<IIOImage> convertBufferedImageToIIOImage(List<BufferedImage> bis) {
        List<IIOImage> oimages = new ArrayList<IIOImage>();
        for (BufferedImage bi : bis) {
            oimages.add(new IIOImage(bi, null, null));
        }

        return oimages;
    }

    /**
     * Constructor.
     *
     * @param imageFile an image file
     * @param index index of images
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param lang language code, which follows ISO 639-3 standard
     */
    public OCRImageEntity(File imageFile, int index, Rectangle rect, String lang) {
        this.imageFile = imageFile;
        this.inputfilename = imageFile.getPath();
        this.index.set(index);
        this.rect = rect;
        this.language.set(lang);
    }

    /**
     * Gets oimages.
     *
     * @return the list of oimages
     */
    public List<IIOImage> getOimages() {
        return oimages;
    }

    /**
     * Gets selected oimages.
     *
     * @return the list of selected oimages
     */
    public List<IIOImage> getSelectedOimages() {
        if (doublesided) {
            List<IIOImage> tempList = new ArrayList<IIOImage>();
            for (IIOImage image : (index.get() == -1 ? oimages : oimages.subList(index.get(), index.get() + 1))) {
                // split image in half
                tempList.addAll(splitImage(image));
            }
            return tempList;
        } else {
            return index.get() == -1 ? oimages : oimages.subList(index.get(), index.get() + 1);
        }
    }

    /**
     * Gets image file.
     *
     * @return the imageFile
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * Gets cloned image files.
     *
     * @return the ClonedImageFiles
     * @throws java.io.IOException
     */
    public List<File> getClonedImageFiles() throws IOException {
        return null;
//        if (oimages != null) {
//            if (dpiX == 0 || dpiY == 0) {
//                if (rect == null || rect.isEmpty()) {
////                    return ImageIOHelper.createTiffFiles(oimages, index);
//                } else {
//                    // rectangular region
////                    BufferedImage bi = ((BufferedImage) oimages.get(index).getRenderedImage()).getSubimage(rect.x, rect.y, rect.width, rect.height);
//                    // On Linux, the standard getSubimage method has generated images that Tesseract does not like.
//                    BufferedImage bi = ImageHelper.getSubImage((BufferedImage) oimages.get(index).getRenderedImage(), rect.x, rect.y, rect.width, rect.height);
//                    List<IIOImage> tempList = new ArrayList<IIOImage>();
//                    tempList.add(new IIOImage(bi, null, null));
//                    return ImageIOHelper.createTiffFiles(tempList, 0);
//                }
//            } else // scaling
//            {
//                if (rect == null || rect.isEmpty()) {
//                    List<IIOImage> tempList = new ArrayList<IIOImage>();
//                    for (IIOImage oimage : (index == -1 ? oimages : oimages.subList(index, index + 1))) {
//                        BufferedImage bi = (BufferedImage) oimage.getRenderedImage();
//                        Map<String, String> metadata = ImageIOHelper.readImageData(oimage);
//                        float scale = dpiX / Float.parseFloat(metadata.get("dpiX"));
//                        bi = ImageHelper.getScaledInstance(bi, (int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale));
//                        tempList.add(new IIOImage(bi, null, null));
//                    }
//                    return ImageIOHelper.createTiffFiles(tempList, (index == -1 ? index : 0), dpiX, dpiY);
//                } else {
//                    // rectangular region
//                    //Cut out the subimage first and rescale that
//                    BufferedImage bi = ((BufferedImage) oimages.get(index).getRenderedImage()).getSubimage(rect.x, rect.y, rect.width, rect.height);
//                    Map<String, String> metadata = ImageIOHelper.readImageData(oimages.get(index));
//                    float scale = dpiX / Float.parseFloat(metadata.get("dpiX"));
//                    bi = ImageHelper.getScaledInstance(bi, (int) (bi.getWidth() * scale), (int) (bi.getHeight() * scale));
//                    List<IIOImage> tempList = new ArrayList<IIOImage>();
//                    tempList.add(new IIOImage(bi, null, null));
//                    return ImageIOHelper.createTiffFiles(tempList, 0, dpiX, dpiY);
//                }
//            }
//        } else {
//            return ImageIOHelper.createTiffFiles(imageFile, index);
//        }
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index.get();
    }
    
    void setIndex(int index) {
        this.index.set(index);
    }

    /**
     * Gets bounding rectangle.
     *
     * @return the bounding rectangle
     */
    public Rectangle getRect() {
        return rect;
    }

    void setRect(Rectangle rect) {
        this.rect = rect;
    }

    /**
     * Sets screenshot mode.
     *
     * @param mode true for resampling the input image; false for no
     * manipulation of the image
     */
    public void setScreenshotMode(boolean mode) {
        dpiX = mode ? 300 : 0;
        dpiY = mode ? 300 : 0;
    }

    /**
     * Sets resolution (DPI).
     *
     * @param dpiX horizontal resolution
     * @param dpiY vertical resolution
     */
    public void setResolution(int dpiX, int dpiY) {
        this.dpiX = dpiX;
        this.dpiY = dpiY;
    }

    /**
     * Gets language code.
     *
     * @return the language code
     */
    public String getLanguage() {
        return language.get();
    }

    public void setLanguage(String lang) {
        language.set(lang);
    }

    /**
     * Gets input filename.
     *
     * @return the input filename
     */
    public String getInputfilename() {
        return inputfilename;
    }

    /**
     * Splits image in halves (as in double-sided pages).
     * 
     * @param image
     * @return two half images
     */
    public List<IIOImage> splitImage(IIOImage image) {
        List<IIOImage> tempList = new ArrayList<IIOImage>();
        RenderedImage ri = image.getRenderedImage();
        Rectangle cropRect = new Rectangle(0, 0, ri.getWidth() / 2, ri.getHeight());
        BufferedImage bi = ImageHelper.getSubImage((BufferedImage) ri, cropRect.x, cropRect.y, cropRect.width, cropRect.height);
        tempList.add(new IIOImage(bi, null, null));
        cropRect = new Rectangle(ri.getWidth() / 2, 0, ri.getWidth() / 2, ri.getHeight());
        bi = ImageHelper.getSubImage((BufferedImage) ri, cropRect.x, cropRect.y, cropRect.width, cropRect.height);
        tempList.add(new IIOImage(bi, null, null));

        return tempList;
    }
}
