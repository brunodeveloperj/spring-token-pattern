package com.mds.token.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import com.mds.error.handler.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagerServiceImplTest {

  @InjectMocks private AuthenticationManagerServiceImpl authenticationManagerService;

  @Test
  void getToken_shouldReturnTokenFromAuthenticatorSingleton() throws GeneralException {
    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    when(mockConfig.getAuthorization()).thenReturn("Bearer access_token");

    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      mocked.when(() -> AuthenticatorSSOConfig.getInstance()).thenReturn(mockConfig);

      Token token = authenticationManagerService.getToken();

      assertNotNull(token);
      assertEquals("Bearer access_token", token.getValue());
    }
  }

  @Test
  void getEncryptedObject_shouldReturnEncryptedObjectFromAuthenticatorSingleton() throws GeneralException {
    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    when(mockConfig.getEncryptedObject()).thenReturn("encrypted_value");

    try (MockedStatic<AuthenticatorSSOConfig> mocked = mockStatic(AuthenticatorSSOConfig.class)) {
      mocked.when(() -> AuthenticatorSSOConfig.getInstance()).thenReturn(mockConfig);

      EncryptedObject encryptedObject = authenticationManagerService.getEncryptedObject();

      assertNotNull(encryptedObject);
      assertEquals("encrypted_value", encryptedObject.getValue());
    }
  }
}
