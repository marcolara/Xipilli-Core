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
 **  @version ecomzero-persistence 1.0
 **  (C) Copyright 2011 Ecomzero.com, All rights reserved.
 **
 **/
package com.xipilli.persistence.model;

import java.util.Date;

/**
 * The contract for all persistent entities.
 */
public interface PersistentEntity {
    //critical property constants
    public static final String STATUS = "status";
    public static final String CREATE_TIMESTAMP = "createTimestamp";
    public static final String UPDATE_TIMESTAMP = "updateTimestamp";

    public String getStatus();

    public void setStatus(String status);

    public Date getCreateTimestamp();

    public void setCreateTimestamp(Date createTimestamp);

    public Date getUpdateTimestamp();

    public void setUpdateTimestamp(Date updateTimestamp);

//    /**
//     * Every entity has a runtime type.  Type is therefore a reserved word with regards to
//     * properties.
//     */
//    public Type getType();

}
