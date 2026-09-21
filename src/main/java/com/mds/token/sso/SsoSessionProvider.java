package com.mds.token.sso;

import com.mds.error.handler.exception.GeneralException;

/**
 * Provider contract for the current SSO session data.
 *
 * <p>Consumers that need the authorization token or the crypto
 * <em>encrypted object</em> must depend on this interface and receive it
 * through dependency injection instead of reaching the
 * {@link com.mds.token.sso.config.AuthenticatorSSOConfig} singleton
 * directly. This keeps provider adapters and application services
 * testable and free of hidden global state.
 *
 * @author MDS
 * @since 0.1.6-SNAPSHOT
 */
public interface SsoSessionProvider {

  /**
   * Tells whether an SSO session has already been established.
   *
   * @return {@code true} when the session exists
   */
  boolean hasSession();

  /**
   * Returns the current authorization token, refreshing it when expired.
   *
   * @return the authorization token
   * @throws GeneralException when the session cannot be obtained
   */
  String getAuthorization() throws GeneralException;

  /**
   * Returns the current encrypted object, recreating it when expired.
   *
   * @return the encrypted object
   * @throws GeneralException when the session cannot be obtained
   */
  String getEncryptedObject() throws GeneralException;
}
