package com.mds.token.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO representing an OAuth 2.0 token response from the SSO endpoint,
 * containing the access token, refresh token, expiration times, token type,
 * session state and scope.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SSOResponseDTO {

  @JsonProperty("access_token") private String accessToken;
  @JsonProperty("expires_in") private int expiresIn;
  @JsonProperty("refresh_expires_in") private int refreshExpiresIn;
  @JsonProperty("refresh_token") private String refreshToken;
  @JsonProperty("token_type") private String tokenType;
  @JsonProperty("not-before-policy") private Long notBeforePolicy;
  @JsonProperty("session_state") private String sessionState;
  @JsonProperty("scope") private String scope;

}