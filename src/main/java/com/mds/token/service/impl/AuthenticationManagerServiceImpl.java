package com.mds.token.service.impl;

import static com.mds.shared.core.pattern.utils.FunctionUtils.executableObject;

import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.sso.SsoSessionProvider;
import com.mds.token.service.AuthenticationService;
import org.springframework.stereotype.Service;

/**
 * {@link AuthenticationService} implementation ({@code "manager"}) that
 * retrieves the bearer token and encrypted object from the injected
 * {@link SsoSessionProvider} session.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Service(value = "manager")
public class AuthenticationManagerServiceImpl implements AuthenticationService {

  private final SsoSessionProvider ssoSessionProvider;

  public AuthenticationManagerServiceImpl(SsoSessionProvider ssoSessionProvider) {
    this.ssoSessionProvider = ssoSessionProvider;
  }

  @Override
  public Token getToken() {
    String authorizationSingleton = executableObject(() -> ssoSessionProvider.getAuthorization());
    return new Token(authorizationSingleton);
  }

  @Override
  public EncryptedObject getEncryptedObject() {
    String encryptedObjectSingleton = executableObject(() -> ssoSessionProvider.getEncryptedObject());
    return new EncryptedObject(encryptedObjectSingleton);
  }

}
