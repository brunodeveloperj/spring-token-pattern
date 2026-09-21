package com.mds.token.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.sso.SsoSessionProvider;
import com.mds.error.handler.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagerServiceImplTest {

  @Mock private SsoSessionProvider ssoSessionProvider;
  @InjectMocks private AuthenticationManagerServiceImpl authenticationManagerService;

  @Test
  void getToken_shouldReturnTokenFromSessionProvider() throws GeneralException {
    when(ssoSessionProvider.getAuthorization()).thenReturn("Bearer access_token");

    Token token = authenticationManagerService.getToken();

    assertNotNull(token);
    assertEquals("Bearer access_token", token.getValue());
  }

  @Test
  void getEncryptedObject_shouldReturnEncryptedObjectFromSessionProvider() throws GeneralException {
    when(ssoSessionProvider.getEncryptedObject()).thenReturn("encrypted_value");

    EncryptedObject encryptedObject = authenticationManagerService.getEncryptedObject();

    assertNotNull(encryptedObject);
    assertEquals("encrypted_value", encryptedObject.getValue());
  }
}
