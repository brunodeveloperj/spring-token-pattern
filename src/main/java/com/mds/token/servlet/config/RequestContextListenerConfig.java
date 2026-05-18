package com.mds.token.servlet.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Auto-configuration that registers a {@link RequestContextListener} bean
 * when running in a Servlet web application and no other instance is
 * already present.
 *
 * <p>Ensures that request-scoped beans (e.g. {@link HttpServletService})
 * can be resolved outside of {@code DispatcherServlet} processing.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class RequestContextListenerConfig {

  @Bean
  @ConditionalOnMissingBean(RequestContextListener.class)
  public RequestContextListener requestContextListener() {
    return new RequestContextListener();
  }

}
