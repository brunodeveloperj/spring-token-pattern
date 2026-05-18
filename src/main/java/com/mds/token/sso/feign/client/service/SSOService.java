package com.mds.token.sso.feign.client.service;


import com.mds.token.sso.dto.SSOResponseDTO;
import com.mds.error.handler.exception.GeneralException;

/**
 * Service interface for obtaining OAuth 2.0 tokens from the SSO identity
 * provider.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public interface SSOService {

  SSOResponseDTO token() throws GeneralException;

}
