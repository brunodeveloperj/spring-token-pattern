package com.mds.token.servlet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class HttpServletServiceImplTest {

  @Mock private ObjectProvider<HttpServletRequest> httpServletRequestProvider;
  @Mock private ObjectProvider<HttpServletResponse> httpServletResponseProvider;
  @Mock private HttpServletRequest httpServletRequest;
  @Mock private HttpServletResponse httpServletResponse;

  private HttpServletServiceImpl httpServletService;

  @BeforeEach
  void setUp() {
    httpServletService = new HttpServletServiceImpl(httpServletRequestProvider, httpServletResponseProvider);
  }

  @Test
  void getRequest_shouldReturnInjectedRequest() {
    when(httpServletRequestProvider.getObject()).thenReturn(httpServletRequest);
    assertEquals(httpServletRequest, httpServletService.getRequest());
  }

  @Test
  void getResponse_shouldReturnInjectedResponse() {
    when(httpServletResponseProvider.getObject()).thenReturn(httpServletResponse);
    assertEquals(httpServletResponse, httpServletService.getResponse());
  }
}
