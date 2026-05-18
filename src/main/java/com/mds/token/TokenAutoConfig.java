package com.mds.token;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring auto-configuration entry point for the {@code spring-token-pattern} library.
 *
 * <p>Enables component scanning on the {@code com.mds.token} package so that
 * all authentication services, SSO handlers, Feign clients, and servlet
 * utilities are automatically registered in the application context when
 * the library is included as a dependency.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@ComponentScan
@ConditionalOnProperty(name = "authentication.enabled", havingValue = "true", matchIfMissing = true)
public class TokenAutoConfig {

}
