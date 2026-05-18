package com.mds.token.service.impl;

import com.mds.token.config.AuthenticationPropertiesConfig;
import com.mds.token.model.EncryptedObject;
import com.mds.token.model.Token;
import com.mds.token.service.AuthenticationService;
import com.mds.token.servlet.service.HttpServletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link AuthenticationService} implementation ({@code "servlet"}) that
 * extracts the bearer token and encrypted object from the current HTTP
 * request headers, using header names configured in
 * {@link AuthenticationPropertiesConfig}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Service(value = "servlet")
public class AuthenticationServletServiceImpl implements AuthenticationService {

  private final HttpServletService httpServletService;
  private final AuthenticationPropertiesConfig authenticationPropertiesConfig;

  @Autowired
  public AuthenticationServletServiceImpl(HttpServletService httpServletService, AuthenticationPropertiesConfig authenticationPropertiesConfig) {
    this.httpServletService = httpServletService;
    this.authenticationPropertiesConfig = authenticationPropertiesConfig;
  }

  @Override
  public Token getToken() {
    String headerToken = httpServletService.getRequest().getHeader(authenticationPropertiesConfig.getHeaderNameToken());
    return new Token(headerToken);
  }

  @Override
  public EncryptedObject getEncryptedObject() {
    String headerEncryptedObject = httpServletService.getRequest().getHeader(authenticationPropertiesConfig.getHeaderNameEncryptedObject());
    return new EncryptedObject(headerEncryptedObject);
  }

}
