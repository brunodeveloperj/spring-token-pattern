package com.mds.token.servlet.service.impl;

import com.mds.token.servlet.service.HttpServletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link HttpServletService} that resolves the
 * current {@link HttpServletRequest} and {@link HttpServletResponse} via
 * Spring {@link ObjectProvider}, ensuring lazy and request-scoped access.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class HttpServletServiceImpl implements HttpServletService {

  private final ObjectProvider<HttpServletRequest> httpServletRequestProvider;
  private final ObjectProvider<HttpServletResponse> httpServletResponseProvider;

  @Autowired
  public HttpServletServiceImpl(ObjectProvider<HttpServletRequest> httpServletRequestProvider,
      ObjectProvider<HttpServletResponse> httpServletResponseProvider) {
    this.httpServletRequestProvider = httpServletRequestProvider;
    this.httpServletResponseProvider = httpServletResponseProvider;
  }

  @Override
  public HttpServletRequest getRequest() {
    return httpServletRequestProvider.getObject();
  }

  @Override
  public HttpServletResponse getResponse() {
    return httpServletResponseProvider.getObject();
  }
}
