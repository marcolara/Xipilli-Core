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

import java.util.regex.Pattern;


/**
 *
 */
public class StringToKeyConverter {
    public static String convert(String s){
        s = s.toLowerCase();
        s = s.replace("-"," "); /* replace - */
        s = s.replaceAll(Pattern.quote("\\n"),""); /* remove new lines */
        s = s.replaceAll(Pattern.quote("\\t"),""); /* remove tabs */
        s = s.replaceAll(Pattern.quote("\\r"),""); /* remove lines */
        s = s.replaceAll("^\\s+",""); /* remove leading whitespace */
        s = s.replaceAll("\\s+$", ""); /* remove trailing whitespace */
        s = s.replaceAll(" {2,}", " "); /* replace multiple whitespaces between words with single blank */
        s = s.replaceAll("\\s", "-"); /* replace spaces with - */
        s = s.replaceAll("[^a-z 0-9 -]",""); /* filter alpha numeric */
        s = s.replace("--","-"); /* replace -- */
        return s;
    }
}
