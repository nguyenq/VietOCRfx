/**
 * Copyright @ 2009 Quan Nguyen
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

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;

public class Utils {

    private static final String EOL = "\n";

    private final static Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Gets the directory of the executing JAR.
     *
     * @param aType
     * @return the directory of the running jar
     */
    public static File getBaseDir(Object aType) {
        URL dir = aType.getClass().getResource("/" + aType.getClass().getName().replaceAll("\\.", "/") + ".class");
        File dbDir = new File(System.getProperty("user.dir"));

        try {
            if (dir.toString().startsWith("jar:")) {
                dir = new URI(dir.toString().replaceFirst("^jar:", "").replaceFirst("/[^/]+.jar!.*$", "")).toURL();
                new File(dir.toURI());
            }
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return dbDir;

//        return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath()); // alternative
    }

    /**
     * Gets filename without extension.
     * http://stackoverflow.com/questions/924394/how-to-get-file-name-without-the-extension
     *
     * @param str
     * @return
     */
    public static String stripExtension(String str) {
        // Handle null case specially.
        if (str == null) {
            return null;
        }

        // Get position of last '.'.
        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.
        if (pos == -1) {
            return str;
        }

        // Otherwise return the string, up to the dot.
        return str.substring(0, pos);
    }

    /**
     * Reads a text file.
     *
     * @param tempTessOutputFile
     * @return
     * @throws Exception
     */
    public static String readTextFile(File tempTessOutputFile) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempTessOutputFile), "UTF-8"))) {
            StringBuilder result = new StringBuilder();
            String str;

            while ((str = in.readLine()) != null) {
                result.append(str).append(EOL);
            }

            int length = result.length();
            if (length >= EOL.length()) {
                result.setLength(length - EOL.length()); // remove last EOL
            }

            return result.toString();
        }
    }

    /**
     * Lists image files recursively in a given directory.
     *
     * @param list
     * @param directory
     */
    public static void listImageFiles(List<File> list, File directory) {
        // list image files and subdir
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().matches(".*\\.(tif|tiff|jpg|jpeg|gif|png|bmp|pdf)$") || file.isDirectory();
            }
        });

        List<File> dirs = new ArrayList<File>();

        // process files first
        for (File file : files) {
            if (file.isFile()) {
                list.add(file);
            } else {
                dirs.add(file);
            }
        }

        // then process directories
        for (File dir : dirs) {
            listImageFiles(list, dir);
        }
    }

    public static List<BufferedImage> getImageList(List<IIOImage> iioImageList) {
        List<BufferedImage> imageList = new ArrayList<BufferedImage>();
        for (IIOImage image : iioImageList) {
            imageList.add((BufferedImage) image.getRenderedImage());
        }
        return imageList;
    }
}
