package com.mds.token.crypto;

import static org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext;

import com.mds.crypto.v1.stub.DLBCryptoLoader;
import com.mds.crypto.v1.stub.DLCrypto;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.mds.crypto.v1.session.DLCryptoSession;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

/**
 * Custom Jackson {@link ValueDeserializer} that transparently decrypts
 * incoming JSON string values using the DLB crypto session.
 *
 * <p>Resolves the {@link DLCrypto} instance from either the
 * {@link AuthenticatorSSOConfig} singleton (if available) or the
 * request-scoped {@link DLCryptoSession}. Supports a configurable
 * retry count via {@code retry.limit}.
 *
 * <p>Usage: annotate DTO fields with
 * {@code @JsonDeserialize(using = DLCryptoDeserialize.class)}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class DLCryptoDeserialize extends ValueDeserializer<String> {

  private static final Logger logger = LoggerFactory.getLogger(DLCryptoDeserialize.class);
  @Value("${retry.limit:0}") private int maxRetry;
  @Lazy @Autowired private DLCryptoSession dlCryptoSession;

  public DLCryptoDeserialize() {
    processInjectionBasedOnCurrentContext(this);
  }

  @Override
  public String deserialize(JsonParser parser, DeserializationContext deserializationContext) {

    // transfer value to the new attribute of the method.
    String value = parser.getValueAsString();

    // checks if the value is null or empty.
    if (value != null && !value.isEmpty()) {

      // validate new value in the deserialize method.
      value = deserialize(value.trim());
    }

    return value;
  }

  private String deserialize(String value) {
    // create attributes of the method.
    boolean retry;
    int counter = 0;
    DLCrypto dlCrypto = null;

    do {
      try {
        String encryptedObject = null;

        // checks that the manager is not null.
        if (AuthenticatorSSOConfig.isExistSingletonInstance()) {
          encryptedObject = AuthenticatorSSOConfig.getInstance().getEncryptedObject();
        }

        // checks that the encryptedObject is null and that the dlCryptoSession is not null.
        if (encryptedObject == null && dlCryptoSession != null) {

          // transfer of session value to dlCrypto.
          dlCrypto = dlCryptoSession.getSession();
        } else if (encryptedObject != null) {

          // deserialize the value contained in encryptedObject.
          dlCrypto = DLBCryptoLoader.deserialize(encryptedObject);
        } else {
          logger.warn("[DLCryptoDeserialize] - Nenhuma fonte de criptografia disponível (encryptedObject e dlCryptoSession são nulos)");
          break;
        }

        // checks that the dlCrypto is not null.
        if (dlCrypto != null) {

          // defines a true value for the client attribute where it will be used to encrypt the passed value.
          dlCrypto.setClient(true);

          // decrypting the value passed on.
          value = dlCrypto.decrypt(value);
        }

        retry = false;

      } catch (Exception ex) {
        if (counter == 0) {
          logger.warn("[DLCryptoDeserialize] - Não foi possível realizar descriptografia do valor. counter = {}, value = {} , exception = {}", counter, value, ex.getMessage());
        }
        counter++;
        retry = counter <= maxRetry;
      }
    } while (retry);

    // return assignment value.
    return value;
  }

}
