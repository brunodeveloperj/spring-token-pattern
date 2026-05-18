package com.mds.token.keys;


import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

/**
 * Utility class containing constant error codes and messages used by the
 * token authentication layer.
 *
 * <p>Error codes {@code ARCAUT_0001} and {@code ARCAUT_0002} are thrown by
 * {@link com.mds.token.sso.feign.client.service.impl.SSOServiceImpl}
 * when the SSO token request fails or returns an invalid response.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = PRIVATE)
public final class MessagesKeys {

  public static final String ARCAUT_0001 = "ARCAUT_0001";
  public static final String ARCAUT_0002 = "ARCAUT_0002";
  public static final String ARCAUT_MESSAGE = "Não foi possível completar a comunicação. Tente novamente mais tarde.";
  public static final String ARCAUT_TITLE = "Ocorreu um erro";

}
