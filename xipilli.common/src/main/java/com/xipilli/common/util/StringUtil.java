/***
 **  @(#) ecomzero.com
 **
 **  Copyright (c) 2011 ecomzero, LLC.  All Rights Reserved.
 **
 **
 **  THIS COMPUTER SOFTWARE IS THE PROPERTY OF ecomzero, LLC.
 **
 **  Permission is granted to use this software as specified by the ecomzero
 **  COMMERCIAL LICENSE AGREEMENT.  You may use this software only for
 **  commercial purposes, as specified in the details of the license.
 **  ecomzero SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY THE LICENSEE
 **  AS A RESULT OF USING OR MODIFYING THIS SOFTWARE IN ANY WAY.
 **
 **  YOU MAY NOT DISTRIBUTE ANY SOURCE CODE OR OBJECT CODE FROM THE
 **  ecomzero.com TOOLKIT AT ANY TIME. VIOLATORS WILL BE PROSECUTED TO THE
 **  FULLEST EXTENT OF UNITED STATES LAW.
 **
 **  @version 1.0
 **  @author Copyright (c) 2011 ecomzero, LLC. All Rights Reserved.
 **
 **/
package com.xipilli.common.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.xipilli.common.CommonsConstants;

/**
 * Util methods for String.
 */
public class StringUtil {
    //support ellipsize calculation
    private final static String NON_THIN = "[^iIl1\\.,']";

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String emptyIfNull(String s) {
        return s == null ? CommonsConstants.EMPTY_STRING : s;
    }

    public static String nullIfEmpty(String s) {
        return isNullOrEmpty(s) ? null : s;
    }

    public static String firstNotNullOrEmpty(String... strings) {
        for (String s : strings) {
            if (!isNullOrEmpty(s)) {
                return s;
            }
        }
        return null;
    }

    public static String[] commaSplit(String toSplit) {
        return toSplit.split(",");
    }

    public static String toCamel(String original) {
        if (original.length() > 1) {
            return new StringBuilder(original.substring(0, 1).toLowerCase()).append(original.substring(1)).toString();
        } else {
            return original.substring(0, 1).toLowerCase();
        }
    }

    public static String toUnderscored(String original) {
        if (original.length() > 1) {
            char[] chars = original.toCharArray();
            StringBuilder buff = new StringBuilder();
            int i = 0;
            for (char c : chars) {
                if (i > 0 && Character.isUpperCase(c)) {
                    buff.append(CommonsConstants.UNDERSCORE_CHAR);
                }
                buff.append(c);
                i++;
            }
            return buff.toString();
        } else {
            return original;
        }
    }

    private static int textWidth(String str) {
        return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
    }

    public static String ellipsize(String text, int max) {

        if (textWidth(text) <= max)
            return text;

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1)
            return text.substring(0, max-3) + "...";

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1)
                newEnd = text.length();

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }

    /**
     * @param s = date string in format: "yyyy-MM-dd".
     */
    public static Date toDate(String s) {
        if (isNullOrEmpty(s)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @param s = date string in format: "yyyy-MM-dd".
     */
    public static Timestamp toTimestamp(String s) {
        if (isNullOrEmpty(s)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dateString = formatter.parse(s);
            return new Timestamp(dateString.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @param date = date string in format: "yyyy-MM-dd".
     * @param time = date string in format: "h:mm a".
     */
    public static Timestamp toTimestamp(String date, String time) {
        if (isNullOrEmpty(date)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm a");
            Date dateString = formatter.parse(new StringBuffer(date).append(" ").append(time).toString());
            return new Timestamp(dateString.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @param s = date string in format: "yyyy-MM-dd".
     */
    public static String toYyyymmdd(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /**
     * @param s = date string in format: "h:mm a".
     */
    public static String toHmma(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(date);
    }

}