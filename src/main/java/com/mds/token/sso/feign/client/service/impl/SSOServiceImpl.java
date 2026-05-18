package com.mds.token.sso.feign.client.service.impl;

import static com.mds.shared.core.pattern.utils.ObjectUtils.isStringBlank;
import static com.mds.token.keys.MessagesKeys.ARCAUT_0001;
import static com.mds.token.keys.MessagesKeys.ARCAUT_0002;
import static com.mds.token.keys.MessagesKeys.ARCAUT_MESSAGE;
import static com.mds.token.keys.MessagesKeys.ARCAUT_TITLE;
import static com.mds.token.keys.SSOKeys.PARAM_CLIENT_ID;
import static com.mds.token.keys.SSOKeys.PARAM_CLIENT_SECRET;
import static com.mds.token.keys.SSOKeys.PARAM_GRANT_TYPE;
import static com.mds.error.handler.enumerator.Action.BACK_HOME;
import static com.mds.error.handler.enumerator.Type.SECURITY;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.mds.token.sso.config.SSOConfig;
import com.mds.token.sso.dto.SSOResponseDTO;
import com.mds.token.sso.feign.client.SSOFeignClient;
import com.mds.token.sso.feign.client.service.SSOService;
import com.mds.error.handler.exception.GeneralException;
import com.mds.error.handler.utils.ErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Default implementation of {@link SSOService} that delegates to
 * {@link SSOFeignClient} for token acquisition.
 *
 * <p>Builds a form-encoded request with client credentials from
 * {@link SSOConfig}, validates the response status and body, and
 * throws a {@link GeneralException} with error code {@code ARCAUT_0001}
 * or {@code ARCAUT_0002} on failure.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j @Service
public class SSOServiceImpl implements SSOService {

  private final SSOConfig ssoConfig;
  private final SSOFeignClient ssoFeignClient;
  private final ErrorUtils errorUtil;

  @Autowired
  public SSOServiceImpl(SSOConfig ssoConfig, SSOFeignClient ssoFeignClient, ErrorUtils errorUtil) {
    this.ssoConfig = ssoConfig;
    this.ssoFeignClient = ssoFeignClient;
    this.errorUtil = errorUtil;
  }

  @Override
  public SSOResponseDTO token() throws GeneralException {
    try {
      ResponseEntity<SSOResponseDTO> exchange = ssoFeignClient.token(createRequest());
      if (exchange.getStatusCode() != OK || !exchange.hasBody()) {
        log.warn("[SSOServiceImpl] - (token): Resposta inválida na obtenção do token. status = {}",
            exchange.getStatusCode());
        errorUtil.throwError(ARCAUT_0001, ARCAUT_MESSAGE, ARCAUT_TITLE, SECURITY, BACK_HOME, UNAUTHORIZED.value());
      }
      SSOResponseDTO response = exchange.getBody();
      if (response == null) {
        log.warn("[SSOServiceImpl] - (token): Corpo da resposta nulo na obtenção do token");
        errorUtil.throwError(ARCAUT_0001, ARCAUT_MESSAGE, ARCAUT_TITLE, SECURITY, BACK_HOME, UNAUTHORIZED.value());
      }
      verifyResponseObject(response);
      return response;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      log.warn("[SSOServiceImpl] - (token): Erro na obtenção do token, message = {} ", e.getMessage());
      errorUtil.throwError(ARCAUT_0001, ARCAUT_MESSAGE, ARCAUT_TITLE, SECURITY, BACK_HOME, UNAUTHORIZED.value());
    }
    return null;
  }

  private void verifyResponseObject(SSOResponseDTO response) throws GeneralException {
    if (isStringBlank(response.getAccessToken())) {
      log.error("[SSOServiceImpl] - (verifyResponseObject): Erro na obtenção do token. Access Token nulo");
      errorUtil.throwError(ARCAUT_0002, ARCAUT_MESSAGE, ARCAUT_TITLE, SECURITY, BACK_HOME, UNAUTHORIZED.value());
    }
  }

  private MultiValueMap<String, String> createRequest() {
    MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add(PARAM_GRANT_TYPE, ssoConfig.getGrantType());
    request.add(PARAM_CLIENT_ID, ssoConfig.getClientId());
    request.add(PARAM_CLIENT_SECRET, ssoConfig.getClientSecret());
    return request;
  }

}
