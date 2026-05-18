package com.mds.token.sso.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

/**
 * Feign encoder configuration that registers a {@link FormEncoder}
 * wrapping the default {@link SpringEncoder}, enabling
 * {@code application/x-www-form-urlencoded} requests via
 * {@link com.mds.token.sso.feign.client.SSOFeignClient}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class CoreFeignConfig {
  @Autowired
  private ObjectProvider<FeignHttpMessageConverters> messageConverters;

  @Bean
  Encoder feignFormEncoder() {
    return new FormEncoder(new SpringEncoder(this.messageConverters));
  }
}
