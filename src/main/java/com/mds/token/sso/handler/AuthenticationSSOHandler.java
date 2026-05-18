package com.mds.token.sso.handler;

import com.mds.crypto.v1.handler.EncryptedObjectHandler;
import com.mds.token.config.AuthenticationPropertiesConfig;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import com.mds.token.sso.feign.client.service.SSOService;
import com.mds.error.handler.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A class that handles authentication using the SSO client.
 * <p>
 * This class provides a method to get the AuthenticatorManagerConfig instance, which manages the authentication token and encrypted object.
 * It also has a `PostConstruct` method that is called after the class is constructed, which attempts to build the AuthenticatorManagerConfig instance and logs a message if it is successful.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j @Component
public class AuthenticationSSOHandler {

  private final SSOService ssoService;
  private final AuthenticationPropertiesConfig authenticationPropertiesConfig;
  private final EncryptedObjectHandler encryptedObjectHandler;

  @Autowired
  public AuthenticationSSOHandler(SSOService ssoService, AuthenticationPropertiesConfig authenticationPropertiesConfig, EncryptedObjectHandler encryptedObjectHandler) {
    this.ssoService = ssoService;
    this.authenticationPropertiesConfig = authenticationPropertiesConfig;
    this.encryptedObjectHandler = encryptedObjectHandler;
  }

  /**
   * Attempts to build the AuthenticatorManagerConfig instance.
   * <p>
   * This method is called after the class is constructed.
   * It attempts to build the AuthenticatorManagerConfig instance and logs a message if it is successful.
   */
  @PostConstruct
  public void initManager() {
    if (authenticationPropertiesConfig.isInitConstructionOfSsoConfig()) {
      try {
        AuthenticatorSSOConfig config = getManager();
        if (config != null) {
          log.info("Authenticator build success.");
        }
      } catch (GeneralException ex) {
        log.error("Authenticator build fail.", ex);
      }
    }
  }

  /**
   * Gets the AuthenticatorManagerConfig instance.
   *
   * @return The AuthenticatorManagerConfig instance, or null if the instance could not be created.
   * @throws GeneralException If an error occurs while getting the AuthenticatorManagerConfig instance.
   */
  public AuthenticatorSSOConfig getManager() throws GeneralException {
    return AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);
  }

}
