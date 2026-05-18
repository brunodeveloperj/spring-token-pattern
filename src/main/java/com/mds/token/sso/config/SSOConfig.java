package com.mds.token.sso.config;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the SSO (SSO / Keycloak) OAuth 2.0
 * client, bound to the {@code authentication.sso.client} prefix.
 *
 * <p>Provides the token endpoint URL, client credentials, and grant type
 * consumed by {@link com.mds.token.sso.feign.client.service.impl.SSOServiceImpl}
 * to obtain access tokens.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "authentication.sso.client")
public class SSOConfig {

  private String accessTokenUrl = EMPTY;
  private String clientId = EMPTY;
  private String clientSecret = EMPTY;
  private String grantType = EMPTY;

}
