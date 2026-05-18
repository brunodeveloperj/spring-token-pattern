package com.mds.token.crypto;

import static org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext;

import com.mds.crypto.v1.stub.DLBCryptoLoader;
import com.mds.crypto.v1.stub.DLCrypto;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import com.mds.crypto.v1.session.DLCryptoSession;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Custom Jackson {@link ValueSerializer} that transparently encrypts
 * outgoing JSON string values using the DLB crypto session.
 *
 * <p>Resolves the {@link DLCrypto} instance from either the
 * {@link AuthenticatorSSOConfig} singleton (if available) or the
 * request-scoped {@link DLCryptoSession}, then encrypts the value
 * before writing it to the JSON output.
 *
 * <p>Usage: annotate DTO fields with
 * {@code @JsonSerialize(using = DLCryptoSerialize.class)}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
public class DLCryptoSerialize extends ValueSerializer<String> {

  private static final Logger logger = LoggerFactory.getLogger(DLCryptoSerialize.class);
  @Autowired private DLCryptoSession dlCryptoSession;

  public DLCryptoSerialize() {
    processInjectionBasedOnCurrentContext(this);
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
    // checks if the value is null or empty.
    if (value == null || value.isEmpty()) {
      gen.writeString(value);
      return;
    }

    try {
      // create attributes of the method.
      DLCrypto dlCrypto;
      String encryptedObject = null;

      // checks that the manager is not null.
      if (AuthenticatorSSOConfig.isExistSingletonInstance()) {
        encryptedObject = AuthenticatorSSOConfig.getInstance().getEncryptedObject();
      }

      // checks that the encryptedObject is null and that the dlCryptoSession is not null.
      if (encryptedObject == null && dlCryptoSession != null) {

        // transfer of session value to dlCrypto.
        dlCrypto = dlCryptoSession.getSession();
        if (dlCrypto == null) {
          logger.error(
              "[DLCryptoSerialize] - Sessão de criptografia indisponível: dlCryptoSession.getSession() retornou nulo");
          throw new RuntimeException(
              "[DLCryptoSerialize] - Não foi possível obter a sessão de criptografia para serializar o valor");
        }
      } else if (encryptedObject != null) {

        // deserialize the value contained in encryptedObject.
        dlCrypto = DLBCryptoLoader.deserialize(encryptedObject);
      } else {
        logger.error(
            "[DLCryptoSerialize] - Nenhuma fonte de criptografia disponível (encryptedObject e dlCryptoSession são nulos)");
        throw new RuntimeException(
            "[DLCryptoSerialize] - Nenhuma fonte de criptografia disponível para serializar o valor");
      }

      if (dlCrypto == null) {
        logger.error("[DLCryptoSerialize] - Instância de criptografia indisponível após resolução da fonte");
        throw new RuntimeException(
            "[DLCryptoSerialize] - Não foi possível inicializar a criptografia para serializar o valor");
      }

      dlCrypto.setClient(true);

      // encrypting the value passed on.
      String encryptedValue = dlCrypto.encrypt(value);

      // rewriting assignment value.
      gen.writeString(encryptedValue);
    } catch (Exception ex) {
      logger.error("[DLCryptoSerialize] - Erro ao realizar serialize do valor", ex);
      throw new RuntimeException("[DLCryptoSerialize] - Erro ao criptografar valor durante serialização", ex);
    }
  }
}
