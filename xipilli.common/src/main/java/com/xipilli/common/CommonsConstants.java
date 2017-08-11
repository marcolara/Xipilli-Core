/***
 **  @(#) ecomzero.com
 **
 **  Copyright (c) 2010 ecomzero, LLC.  All Rights Reserved.
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
 **  @author Copyright (c) 2010 ecomzero, LLC. All Rights Reserved.
 **
 **/
package com.xipilli.common;

import java.text.SimpleDateFormat;

/**
 * Super indepedent and common constants.
 */
public interface CommonsConstants {

	public static final String EMPTY_STRING = "";
	public static final String ELLIPSIS = "...";
	public static final String NEWLINE = "\r\n";

	public static final String UNDERSCORE = "_";
	public static final char UNDERSCORE_CHAR = '_';

	public static final String QUESTIONMARK = "?";
	public static final char QUESTIONMARK_CHAR = '?';

	public static final String AMPERSAND = "&";
	public static final char AMPERSAND_CHAR = '&';

	public static final String PERCENTAGE = "%";
	public static final char PERCENTAGE_CHAR = '%';

	public static final String EQUALS = "=";
	public static final char EQUALS_CHAR = '=';

	public static final String HASHMARK = "#";
	public static final char HASHMARK_CHAR = '#';

	public static final String HTTP_COLON_SLASH_SLASH = "http://";

	public static final SimpleDateFormat COMPOUND_DATEFORMAT = new SimpleDateFormat("MMddyyyyHHmmss");

	public static final long MILLIS_IN_MIN = 60 * 1000;
	public static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MIN;
	public static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;

	public static final String TEXT_PLAIN = "text/plain";
}