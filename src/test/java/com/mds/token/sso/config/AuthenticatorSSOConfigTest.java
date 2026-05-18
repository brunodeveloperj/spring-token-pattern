package com.mds.token.sso.config;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mds.crypto.v1.handler.EncryptedObjectHandler;
import com.mds.token.sso.dto.SSOResponseDTO;
import com.mds.token.sso.feign.client.service.SSOService;
import com.mds.error.handler.exception.GeneralException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticatorSSOConfigTest {

  @Mock private SSOService ssoService;
  @Mock private EncryptedObjectHandler encryptedObjectHandler;

  @BeforeEach
  void setUp() throws Exception {
    resetSingleton();
    SSOResponseDTO tokenResponse = new SSOResponseDTO(
        "access_token", 300, 1800, "refresh_token", "Bearer", 0L, "session", "openid");
    lenient().when(ssoService.token()).thenReturn(tokenResponse);
    lenient().when(encryptedObjectHandler.create()).thenReturn("encrypted_value");
  }

  @AfterEach
  void tearDown() throws Exception {
    resetSingleton();
  }

  private void resetSingleton() throws Exception {
    Field instanceField = AuthenticatorSSOConfig.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
    Field runTokenAsyncField = AuthenticatorSSOConfig.class.getDeclaredField("runTokenAsync");
    runTokenAsyncField.setAccessible(true);
    ((AtomicBoolean) runTokenAsyncField.get(null)).set(false);
  }

  @Test
  void build_shouldCreateInstanceAndPopulateTokenAndEncryptedObject() throws GeneralException {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    assertNotNull(config);
    assertEquals("access_token", config.getAuthorization());
    assertEquals("refresh_token", config.getRefreshToken());
    assertEquals("encrypted_value", config.getEncryptedObject());
  }

  @Test
  void build_shouldReturnExistingSingletonOnSubsequentCalls() throws GeneralException {
    AuthenticatorSSOConfig first = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);
    AuthenticatorSSOConfig second = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    assertSame(first, second);
    verify(ssoService, times(1)).token();
  }

  @Test
  void getInstance_shouldReturnNullWhenNoInstanceHasBeenBuilt() throws GeneralException {
    assertNull(AuthenticatorSSOConfig.getInstance());
  }

  @Test
  void getInstance_shouldReturnExistingInstanceAfterBuild() throws GeneralException {
    AuthenticatorSSOConfig built = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    AuthenticatorSSOConfig retrieved = AuthenticatorSSOConfig.getInstance();

    assertNotNull(retrieved);
    assertSame(built, retrieved);
  }

  @Test
  void isExistSingletonInstance_shouldReturnFalseBeforeBuild() {
    assertFalse(AuthenticatorSSOConfig.isExistSingletonInstance());
  }

  @Test
  void isExistSingletonInstance_shouldReturnTrueAfterBuild() throws GeneralException {
    AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    assertTrue(AuthenticatorSSOConfig.isExistSingletonInstance());
  }

  @Test
  void isTokenExpire_shouldReturnFalseForFreshlyBuiltInstance() throws GeneralException {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    assertFalse(config.isTokenExpire());
  }

  @Test
  void isTokenExpire_shouldReturnTrueWhenTokenExpirationIsInThePast()
      throws GeneralException, NoSuchFieldException, IllegalAccessException {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field tokenExpirationField = AuthenticatorSSOConfig.class.getDeclaredField("tokenExpiration");
    tokenExpirationField.setAccessible(true);
    tokenExpirationField.set(config, now().minusSeconds(400));

    assertTrue(config.isTokenExpire());
  }

  @Test
  void getInstance_shouldRefreshTokenSynchronouslyWhenTokenIsExpired() throws Exception {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field tokenExpirationField = AuthenticatorSSOConfig.class.getDeclaredField("tokenExpiration");
    tokenExpirationField.setAccessible(true);
    tokenExpirationField.set(config, now().minusSeconds(400));

    AuthenticatorSSOConfig.getInstance();

    verify(ssoService, times(2)).token();
  }

  @Test
  void getInstance_shouldNotThrowWhenTokenNearExpiryAndTriggersAsyncRefresh() throws Exception {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field tokenExpirationField = AuthenticatorSSOConfig.class.getDeclaredField("tokenExpiration");
    tokenExpirationField.setAccessible(true);
    tokenExpirationField.set(config, now().minusSeconds(290));

    assertDoesNotThrow(() -> AuthenticatorSSOConfig.getInstance());
  }

  @Test
  void getInstance_shouldRegenerateEncryptedObjectWhenItIsNull() throws Exception {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field encryptedObjectField = AuthenticatorSSOConfig.class.getDeclaredField("encryptedObject");
    encryptedObjectField.setAccessible(true);
    encryptedObjectField.set(config, null);

    AuthenticatorSSOConfig.getInstance();

    verify(encryptedObjectHandler, times(2)).create();
  }

  @Test
  void getInstance_shouldRegenerateEncryptedObjectWhenExpirationIsNull() throws Exception {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field encryptedObjectExpirationField = AuthenticatorSSOConfig.class.getDeclaredField("encryptedObjectExpiration");
    encryptedObjectExpirationField.setAccessible(true);
    encryptedObjectExpirationField.set(config, null);

    AuthenticatorSSOConfig.getInstance();

    verify(encryptedObjectHandler, times(2)).create();
  }

  @Test
  void getInstance_shouldRegenerateEncryptedObjectWhenExpiredByMoreThanOneDay() throws Exception {
    AuthenticatorSSOConfig config = AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler);

    Field encryptedObjectExpirationField = AuthenticatorSSOConfig.class.getDeclaredField("encryptedObjectExpiration");
    encryptedObjectExpirationField.setAccessible(true);
    encryptedObjectExpirationField.set(config, now().minusDays(2));

    AuthenticatorSSOConfig.getInstance();

    verify(encryptedObjectHandler, times(2)).create();
  }
}
