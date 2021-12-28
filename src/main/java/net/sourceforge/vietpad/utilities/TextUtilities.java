package net.sourceforge.vietpad.utilities;

import java.text.BreakIterator;
import java.util.regex.*;
import javafx.scene.control.IndexRange;

/**
 *  Text utilities.
 *
 *@author     Quan Nguyen
 *@author     Gero Herrmann
 *@version    1.2, 21 October 2018
 */
public class TextUtilities {

    /**
     * Changes letter case.
     * @param text
     * @param typeOfCase
     * @return
     */
    public static String changeCase(String text, String typeOfCase) {
        String result;

        if (typeOfCase.equals("UPPERCASE")) {
            result = text.toUpperCase();
        } else if (typeOfCase.equals("lowercase")) {
            result = text.toLowerCase();
        } else if (typeOfCase.equals("Title_Case")) {
            StringBuilder strB = new StringBuilder(text.toLowerCase());
            Pattern pattern = Pattern.compile("(?<!\\p{InCombiningDiacriticalMarks}|\\p{L})\\p{L}");
            // word boundary
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                int index = matcher.start();
                strB.setCharAt(index, Character.toTitleCase(strB.charAt(index)));
            }
            result = strB.toString();
        } else if (typeOfCase.equals("Sentence_case")) {
            StringBuilder strB = new StringBuilder(text.toUpperCase().equals(text) ? text.toLowerCase() : text);
            Matcher matcher = Pattern.compile("\\p{L}(\\p{L}+)").matcher(text);
            while (matcher.find()) {
                if (!(matcher.group(0).toUpperCase().equals(matcher.group(0))
                        || matcher.group(1).toLowerCase().equals(matcher.group(1)))) {
                    for (int i = matcher.start(); i < matcher.end(); i++) {
                        strB.setCharAt(i, Character.toLowerCase(strB.charAt(i)));
                    }
                }
            }
            final String QUOTE = "\"'`,<>\u00AB\u00BB\u2018-\u203A";
            matcher = Pattern.compile("(?:[.?!\u203C-\u2049][])}"
                    + QUOTE + "]*|^|\n|:\\s+["
                    + QUOTE + "])[-=_*\u2010-\u2015\\s]*["
                    + QUOTE + "\\[({]*\\p{L}").matcher(text);
            // begin of a sentence
            while (matcher.find()) {
                int i = matcher.end() - 1;
                strB.setCharAt(i, Character.toUpperCase(strB.charAt(i)));
            }
            result = strB.toString();
        } else {
            result = text;
        }

        return result;
    }

    /**
     * Removes line breaks.
     *
     * @param text
     * @param removeSoftHyphens
     * @return
     */
    public static String removeLineBreaks(String text, boolean removeSoftHyphens) {
        text = text.replaceAll("(?<=\n|^)[\t ]+|[\t ]+(?=$|\n)", "");
        text = text.replaceAll("(?<=.)(-|\u2010|\u2011|\u2012|\u2013|\u2014|\u2015|\u00AD)\n(?=.)", "$1");
        text = text.replaceAll("(?<=.)\n(?=.)", " ");
        if (removeSoftHyphens) {
            text = text.replaceAll("\u00AD", "");
        }
        return text;
    }
        
    /**
     * Gets boundaries of the word at specified position.
     * 
     * @param text source text
     * @param pos position in source text
     * @return index range
     */
    public static IndexRange getWordBoundaries(String text, int pos) {
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        int end = boundary.following(pos);
        int start = boundary.previous();
        return new IndexRange(start, end);
    }
    
//    /**
//     * Gets word start.
//     * 
//     * @param text
//     * @param pos
//     * @return 
//     */    
//    public static int getWordStart(String text, int pos) {
//        int index;
//        for (index = pos; index >= 0 && !Character.isWhitespace(text.charAt(index)); index--);
//        return index + 1;
//    }
//
//    /**
//     * Gets word end.
//     * @param text
//     * @param pos
//     * @return 
//     */
//    public static int getWordEnd(String text, int pos) {
//        int index;
//        for (index = pos; index < text.length() && !Character.isWhitespace(text.charAt(index)); index++);
//        return index;
//    }
}
