package com.mds.token.service.impl;

import static com.mds.shared.core.pattern.utils.FunctionUtils.executableObject;

import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import com.mds.token.service.AuthenticationService;
import com.mds.error.handler.exception.GeneralException;
import org.springframework.stereotype.Service;

/**
 * {@link AuthenticationService} implementation ({@code "manager"}) that
 * retrieves the bearer token and encrypted object from the
 * {@link AuthenticatorSSOConfig} singleton managed by the SSO handler.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Service(value = "manager")
public class AuthenticationManagerServiceImpl implements AuthenticationService {

  private AuthenticatorSSOConfig getAuthenticatorSSOConfig() {
    try {
      AuthenticatorSSOConfig authenticatorSSOConfig = AuthenticatorSSOConfig.getInstance();
      if (authenticatorSSOConfig == null) {
        throw new IllegalStateException(
            "AuthenticatorSSOConfig has not been initialized. Ensure build() is called before requesting authentication data.");
      }
      return authenticatorSSOConfig;
    } catch (GeneralException ex) {
      throw new IllegalStateException("Failed to obtain AuthenticatorSSOConfig instance.", ex);
    }
  }

  @Override
  public Token getToken() {
    String authorizationSingleton = executableObject(() -> getAuthenticatorSSOConfig().getAuthorization());
    return new Token(authorizationSingleton);
  }

  @Override
  public EncryptedObject getEncryptedObject() {
    String encryptedObjectSingleton = executableObject(() -> getAuthenticatorSSOConfig().getEncryptedObject());
    return new EncryptedObject(encryptedObjectSingleton);
  }

}
