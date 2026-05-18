package com.mds.token.service;


import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;

/**
 * Contract for retrieving the current authentication artefacts — the
 * bearer {@link Token} and the serialised {@link EncryptedObject}.
 *
 * <p>Two implementations exist:
 * <ul>
 *   <li>{@code "manager"} — obtains data from the
 *       {@link com.mds.token.sso.config.AuthenticatorSSOConfig} singleton</li>
 *   <li>{@code "servlet"} — extracts data from HTTP request headers</li>
 * </ul>
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public interface AuthenticationService {

  Token getToken();
  EncryptedObject getEncryptedObject();

}
