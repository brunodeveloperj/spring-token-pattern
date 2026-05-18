package com.mds.token.crypto;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mds.crypto.v1.stub.DLBCryptoLoader;
import com.mds.crypto.v1.stub.DLCrypto;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import com.mds.crypto.v1.session.DLCryptoSession;
import com.mds.token.sso.config.AuthenticatorSSOConfig;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DLCryptoSerializeTest {

  @Mock private DLCryptoSession dlCryptoSession;
  @Mock private JsonGenerator jsonGenerator;
  @Mock private SerializationContext serializationContext;

  private DLCryptoSerialize serializer;

  @BeforeEach
  void setUp() throws Exception {
    serializer = new DLCryptoSerialize();
    Field field = DLCryptoSerialize.class.getDeclaredField("dlCryptoSession");
    field.setAccessible(true);
    field.set(serializer, dlCryptoSession);
  }

  @Test
  void serialize_shouldWriteNullValueDirectly() throws Exception {
    serializer.serialize(null, jsonGenerator, serializationContext);

    verify(jsonGenerator).writeString((String) null);
  }

  @Test
  void serialize_shouldWriteEmptyValueDirectly() throws Exception {
    serializer.serialize("", jsonGenerator, serializationContext);

    verify(jsonGenerator).writeString("");
  }

  @Test
  void serialize_shouldEncryptValueUsingEncryptedObjectFromConfig() throws Exception {
    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    when(mockConfig.getEncryptedObject()).thenReturn("enc_obj");

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(mockDlCrypto.encrypt(anyString())).thenReturn("encrypted_result");

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class);
        MockedStatic<DLBCryptoLoader> mockedLoader = mockStatic(DLBCryptoLoader.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(true);
      mockedConfig.when(() -> AuthenticatorSSOConfig.getInstance()).thenReturn(mockConfig);
      mockedLoader.when(() -> DLBCryptoLoader.deserialize(anyString())).thenReturn(mockDlCrypto);

      serializer.serialize("plain_value", jsonGenerator, serializationContext);

      verify(mockDlCrypto).setClient(true);
      verify(jsonGenerator).writeString("encrypted_result");
    }
  }

  @Test
  void serialize_shouldEncryptValueUsingSessionWhenNoConfigInstance() throws Exception {
    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(dlCryptoSession.getSession()).thenReturn(mockDlCrypto);
    when(mockDlCrypto.encrypt(anyString())).thenReturn("encrypted_result");

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      serializer.serialize("plain_value", jsonGenerator, serializationContext);

      verify(mockDlCrypto).setClient(true);
      verify(jsonGenerator).writeString("encrypted_result");
    }
  }

  @Test
  void serialize_shouldThrowIOExceptionWhenBothEncryptionSourcesAreNull() throws Exception {
    Field field = DLCryptoSerialize.class.getDeclaredField("dlCryptoSession");
    field.setAccessible(true);
    field.set(serializer, null);

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      assertThrows(RuntimeException.class, () -> serializer.serialize("plain_value", jsonGenerator, serializationContext));
    }
  }

  @Test
  void serialize_shouldThrowRuntimeExceptionOnEncryptionException() throws Exception {
    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(dlCryptoSession.getSession()).thenReturn(mockDlCrypto);
    when(mockDlCrypto.encrypt(anyString())).thenThrow(new RuntimeException("Encryption failed"));

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      assertThrows(RuntimeException.class, () -> serializer.serialize("plain_value", jsonGenerator, serializationContext));
  }
}
}
