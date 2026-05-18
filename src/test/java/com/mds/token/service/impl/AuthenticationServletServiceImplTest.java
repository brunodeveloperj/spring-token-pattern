package com.mds.token.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.mds.token.config.AuthenticationPropertiesConfig;
import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.servlet.service.HttpServletService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServletServiceImplTest {

  @Mock private HttpServletService httpServletService;
  @Mock private AuthenticationPropertiesConfig authenticationPropertiesConfig;
  @Mock private HttpServletRequest httpServletRequest;
  @InjectMocks private AuthenticationServletServiceImpl service;

  @Test
  void getToken_shouldReturnTokenFromRequestHeader() {
    when(authenticationPropertiesConfig.getHeaderNameToken()).thenReturn("Authorization");
    when(httpServletService.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

    Token token = service.getToken();

    assertNotNull(token);
    assertEquals("Bearer token123", token.getValue());
  }

  @Test
  void getToken_shouldReturnTokenWithNullValueWhenHeaderIsAbsent() {
    when(authenticationPropertiesConfig.getHeaderNameToken()).thenReturn("Authorization");
    when(httpServletService.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

    Token token = service.getToken();

    assertNotNull(token);
    assertNull(token.getValue());
  }

  @Test
  void getEncryptedObject_shouldReturnEncryptedObjectFromRequestHeader() {
    when(authenticationPropertiesConfig.getHeaderNameEncryptedObject()).thenReturn("X-Encrypted-Object");
    when(httpServletService.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeader("X-Encrypted-Object")).thenReturn("encrypted_value");

    EncryptedObject encryptedObject = service.getEncryptedObject();

    assertNotNull(encryptedObject);
    assertEquals("encrypted_value", encryptedObject.getValue());
  }

  @Test
  void getEncryptedObject_shouldReturnEncryptedObjectWithNullValueWhenHeaderIsAbsent() {
    when(authenticationPropertiesConfig.getHeaderNameEncryptedObject()).thenReturn("X-Encrypted-Object");
    when(httpServletService.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeader("X-Encrypted-Object")).thenReturn(null);

    EncryptedObject encryptedObject = service.getEncryptedObject();

    assertNotNull(encryptedObject);
    assertNull(encryptedObject.getValue());
  }
}
