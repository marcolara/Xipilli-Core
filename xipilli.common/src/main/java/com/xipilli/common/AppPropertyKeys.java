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
 **  (C) Copyright 2011 Ecomzero.com, All rights reserved.
 **
 **/
package com.xipilli.common;

/**
 * @author malara
 *
 */
public interface AppPropertyKeys {
	// authorization
	public static final String SECURITY_AUTHORIZATION_OAUTH_AUTHORIZEDINTERNALCLIENT_ID = "${security.authorization.oauth.authorizedInternalClient.id}";
	public static final String SECURITY_AUTHORIZATION_OAUTH_AUTHORIZEDINTERNALCLIENT_GRANTTYPE = "${security.authorization.oauth.authorizedInternalClient.grantType}";
	public static final String SECURITY_AUTHORIZATION_OAUTH_AUTHORIZEDINTERNALCLIENT_DEFAULTUSERNAME = "${security.authorization.oauth.authorizedInternalClient.defaultUserName}";
	public static final String SECURITY_AUTHORIZATION_OAUTH_AUTHORIZEDINTERNALCLIENT_DEFAULTUSERAUTHORITY = "${security.authorization.oauth.authorizedInternalClient.defaultUserAuthority}";

	// misc config
	public static final String MESSAGE_CACHESECONDS = "${message.cacheSeconds}";
	public static final String USER_ACTIVATION_EMAIL_FROM_ADDRESS = "${user.activation.email.from.address}";
	public static final String PASSWORD_RECOVERY_EMAIL_FROM_ADDRESS = "${password.recovery.email.from.address}";

	// authentication (user password/token creation)
	public static final String SECURITY_AUTHENTICATION_REMEMBERME_KEY = "${security.authentication.rememberme.key}";
	public static final String SECURITY_AUTHENTICATION_PASSWORD_SALT_KEY = "${security.authentication.password.salt.key}";
	public static final String SECURITY_USER_TOKEN_EXPIRATION_HOURS = "${security.user.token.expiration.hours}";
	public static final String SECURITY_USER_TOKEN_SECRETPHRASE1_KEY = "${security.user.token.secretPhrase1.key}";
	public static final String SECURITY_USER_TOKEN_SECRETPHRASE2_KEY = "${security.user.token.secretPhrase2.key}";
	public static final String SECURITY_LOGOUT_URL = "${security.logout.url}";
	public static final String SECURITY_LOGOUT_REDIRECTPARAM = "${security.logout.redirectParam}";
}
