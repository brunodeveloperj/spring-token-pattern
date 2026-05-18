package com.mds.token.sso.feign.client.service.impl;

import static com.mds.token.keys.MessagesKeys.ARCAUT_0001;
import static com.mds.token.keys.MessagesKeys.ARCAUT_0002;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.mds.token.sso.config.SSOConfig;
import com.mds.token.sso.dto.SSOResponseDTO;
import com.mds.token.sso.feign.client.SSOFeignClient;
import com.mds.error.handler.exception.GeneralException;
import com.mds.error.handler.utils.ErrorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SSOServiceImplTest {

  @Mock private SSOConfig ssoConfig;
  @Mock private SSOFeignClient ssoFeignClient;
  @Mock private ErrorUtils errorUtil;
  @InjectMocks private SSOServiceImpl ssoService;

  private SSOResponseDTO validResponse;

  @BeforeEach
  void setUp() {
    validResponse = new SSOResponseDTO(
        "access_token_value", 300, 1800, "refresh_token_value", "Bearer", 0L, "session", "openid");
  }

  @Test
  void token_shouldReturnResponseWhenFeignReturnsOkWithValidBody() throws GeneralException {
    when(ssoFeignClient.token(any())).thenReturn(ResponseEntity.ok(validResponse));

    SSOResponseDTO result = ssoService.token();

    assertNotNull(result);
    assertEquals("access_token_value", result.getAccessToken());
    assertEquals("refresh_token_value", result.getRefreshToken());
  }

  @Test
  void token_shouldBuildRequestUsingSsoConfigValues() throws GeneralException {
    when(ssoConfig.getGrantType()).thenReturn("client_credentials");
    when(ssoConfig.getClientId()).thenReturn("my-client");
    when(ssoConfig.getClientSecret()).thenReturn("my-secret");
    when(ssoFeignClient.token(any())).thenReturn(ResponseEntity.ok(validResponse));

    ssoService.token();

    verify(ssoConfig).getGrantType();
    verify(ssoConfig).getClientId();
    verify(ssoConfig).getClientSecret();
  }

  @Test
  void token_shouldInvokeErrorUtilWhenResponseStatusIsNotOk() throws GeneralException {
    when(ssoFeignClient.token(any())).thenReturn(ResponseEntity.status(UNAUTHORIZED).build());

    ssoService.token();

    verify(errorUtil, atLeastOnce()).throwError(
        eq(ARCAUT_0001), any(), any(), any(), any(), eq(UNAUTHORIZED.value()));
  }

  @Test
  void token_shouldInvokeErrorUtilWhenResponseBodyIsNull() throws GeneralException {
    when(ssoFeignClient.token(any())).thenReturn(ResponseEntity.<SSOResponseDTO>ok().build());

    ssoService.token();

    verify(errorUtil, atLeastOnce()).throwError(
        eq(ARCAUT_0001), any(), any(), any(), any(), eq(UNAUTHORIZED.value()));
  }

  @Test
  void token_shouldInvokeErrorUtilWhenAccessTokenIsBlank() throws GeneralException {
    SSOResponseDTO blankTokenResponse = new SSOResponseDTO(
        "", 300, 1800, "refresh", "Bearer", 0L, "session", "openid");
    when(ssoFeignClient.token(any())).thenReturn(ResponseEntity.ok(blankTokenResponse));

    ssoService.token();

    verify(errorUtil).throwError(
        eq(ARCAUT_0002), any(), any(), any(), any(), eq(UNAUTHORIZED.value()));
  }

  @Test
  void token_shouldInvokeErrorUtilWhenFeignClientThrowsRuntimeException() throws GeneralException {
    when(ssoFeignClient.token(any())).thenThrow(new RuntimeException("Connection refused"));

    ssoService.token();

    verify(errorUtil).throwError(
        eq(ARCAUT_0001), any(), any(), any(), any(), anyInt());
  }
}
