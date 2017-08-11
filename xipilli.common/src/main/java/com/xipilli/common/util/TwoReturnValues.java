/***
 **  @(#) ecomzero.com
 **
 **  (C) Copyright 2011 Ecomzero.com, All rights reserved.
 **
 **
 **  THIS COMPUTER SOFTWARE IS THE PROPERTY OF Ecomzero.
 **
 **  This program code and all derivatives thereof are the sole property of
 **  Ecomzero.com.  Recipient and/or user, by accepting this source
 **  code, agrees that neither this source code nor any part thereof
 **  shall be reproduced, copied, adapted, distributed, used, displayed
 **  or transferred to any party, or used or disclosed to others for
 **  development, consulting, or any other purpose except as specifically
 **   authorized in writing by Ecomzero.com.
 **
 **  @version ecomzero-common 1.0
 **  (C) Copyright 2012 Ecomzero.com, All rights reserved.
 **
 **/
package com.xipilli.common.util;

/**
 * @author malara
 * @param <R>
 * @param <S>
 *
 */
public class TwoReturnValues<R,S> {
	private R	first;
	private S	second;

	public TwoReturnValues(R first, S second) {
		this.first = first;
		this.second = second;
	}

	public R getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}
}
