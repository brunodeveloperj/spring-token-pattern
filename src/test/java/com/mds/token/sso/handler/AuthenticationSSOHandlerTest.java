package com.mds.token.sso.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.mds.crypto.v1.handler.EncryptedObjectHandler;
import com.mds.token.config.AuthenticationPropertiesConfig;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import com.mds.token.sso.feign.client.service.SSOService;
import com.mds.error.handler.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationSSOHandlerTest {

  @Mock private SSOService ssoService;
  @Mock private AuthenticationPropertiesConfig authenticationPropertiesConfig;
  @Mock private EncryptedObjectHandler encryptedObjectHandler;
  @InjectMocks private AuthenticationSSOHandler authenticationSSOHandler;

  @Test
  void initManager_shouldCallBuildWhenInitConstructionIsTrue() throws GeneralException {
    when(authenticationPropertiesConfig.isInitConstructionOfSsoConfig()).thenReturn(true);
    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      mocked.when(() -> AuthenticatorSSOConfig.build(any(), any()))
          .thenReturn(mock(AuthenticatorSSOConfig.class));

      authenticationSSOHandler.initManager();

      mocked.verify(() -> AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler));
    }
  }

  @Test
  void initManager_shouldNotCallBuildWhenInitConstructionIsFalse() {
    when(authenticationPropertiesConfig.isInitConstructionOfSsoConfig()).thenReturn(false);
    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      authenticationSSOHandler.initManager();

      mocked.verify(() -> AuthenticatorSSOConfig.build(any(), any()), never());
    }
  }

  @Test
  void initManager_shouldNotPropagateGeneralExceptionWhenBuildFails() {
    when(authenticationPropertiesConfig.isInitConstructionOfSsoConfig()).thenReturn(true);
    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      mocked.when(() -> AuthenticatorSSOConfig.build(any(), any()))
          .thenThrow(GeneralException.class);

      assertDoesNotThrow(() -> authenticationSSOHandler.initManager());
    }
  }

  @Test
  void getManager_shouldDelegateToBuildWithInjectedDependencies() throws GeneralException {
    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      mocked.when(() -> AuthenticatorSSOConfig.build(any(), any())).thenReturn(mockConfig);

      AuthenticatorSSOConfig result = authenticationSSOHandler.getManager();

      assertEquals(mockConfig, result);
      mocked.verify(() -> AuthenticatorSSOConfig.build(ssoService, encryptedObjectHandler));
    }
  }
}
