package com.mds.token.sso.feign.client;

import com.mds.token.sso.config.CoreFeignConfig;
import com.mds.token.sso.dto.SSOResponseDTO;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign client for the SSO (SSO / Keycloak) token endpoint.
 *
 * <p>Sends {@code application/x-www-form-urlencoded} POST requests with
 * OAuth 2.0 client credentials and returns an {@link SSOResponseDTO}
 * containing the access and refresh tokens.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@FeignClient(
    value = "sso-client",
    url = "${authentication.sso.client.accessTokenUrl}",
    configuration = CoreFeignConfig.class)
public interface SSOFeignClient {

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  ResponseEntity<SSOResponseDTO> token(Map<String, ?> formParams);

}
