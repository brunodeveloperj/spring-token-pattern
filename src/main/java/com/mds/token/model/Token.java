package com.mds.token.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Model representing a bearer token (typically a JWT) obtained from the
 * SSO identity provider or extracted from HTTP request headers.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Token extends AuthBase {

  public Token(String value) {
    super(value);
  }

}
