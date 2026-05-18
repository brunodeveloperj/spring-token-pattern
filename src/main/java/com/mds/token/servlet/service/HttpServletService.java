package com.mds.token.servlet.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstraction for accessing the current {@link HttpServletRequest} and
 * {@link HttpServletResponse} in a request-scoped context.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public interface HttpServletService {

  HttpServletRequest getRequest();
  HttpServletResponse getResponse();

}
