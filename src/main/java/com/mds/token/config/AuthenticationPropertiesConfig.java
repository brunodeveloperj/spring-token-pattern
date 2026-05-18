package com.mds.token.config;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the authentication module, bound to the
 * {@code authentication.config} prefix.
 *
 * <p>Controls whether the {@link com.mds.token.sso.config.AuthenticatorSSOConfig}
 * singleton is eagerly initialised at startup
 * ({@code initConstructionOfSsoConfig}) and which HTTP header names
 * carry the bearer token ({@code headerNameToken}) and encrypted object
 * ({@code headerNameEncryptedObject}) in servlet-based authentication.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "authentication.config")
public class AuthenticationPropertiesConfig {

  private boolean initConstructionOfSsoConfig = false;
  private String headerNameToken = EMPTY;
  private String headerNameEncryptedObject = EMPTY;

}
