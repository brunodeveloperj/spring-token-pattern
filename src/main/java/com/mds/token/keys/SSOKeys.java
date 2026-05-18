package com.mds.token.keys;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class containing constant parameter names used when building
 * the OAuth 2.0 form-encoded request to the SSO token endpoint.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SSOKeys {

  public static final String PARAM_GRANT_TYPE = "grant_type";
  public static final String PARAM_CLIENT_ID = "client_id";
  public static final String PARAM_CLIENT_SECRET = "client_secret";

}
