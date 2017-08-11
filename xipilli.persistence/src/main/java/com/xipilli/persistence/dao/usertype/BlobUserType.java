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
package com.xipilli.persistence.dao.usertype;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.rowset.serial.SerialBlob;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Component;

/**
 * @author malara
 * 
 */
@Component
public class BlobUserType implements UserType {

	private static final int[]	SQL_TYPES	= {Types.BLOB};

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		if (value == null) {
			return null;
		} else {
			byte[] bytes = (byte[]) value;
			byte[] result = new byte[bytes.length];
			System.arraycopy(bytes, 0, result, 0, bytes.length);
			return result;
		}
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class returnedClass() {
		return byte[].class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet,
	 * java.lang.String[], org.hibernate.engine.spi.SessionImplementor,
	 * java.lang.Object)
	 */
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
		Blob blob = rs.getBlob(names[0]);
		return blob.getBytes(1, (int) blob.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
	 * java.lang.Object, int, org.hibernate.engine.spi.SessionImplementor)
	 */
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		st.setBlob(index, new SerialBlob((byte[]) value));
	}
}
