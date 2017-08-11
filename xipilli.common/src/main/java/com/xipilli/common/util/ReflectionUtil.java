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

/**
 * Util methods for reflection.
 */
public class ReflectionUtil {

    public static StringBuilder toTypeNameBuilder(String typeName) {
        StringBuilder properTypeName;
        if (typeName.length() > 1) {
            properTypeName = new StringBuilder(typeName.substring(0,1).toUpperCase()).append(typeName.substring(1));
        } else {
            properTypeName = new StringBuilder(typeName.toUpperCase());
        }
        return properTypeName;
    }

}
