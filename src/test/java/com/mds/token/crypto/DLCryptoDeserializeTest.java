package com.mds.token.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mds.crypto.v1.stub.DLBCryptoLoader;
import com.mds.crypto.v1.stub.DLCrypto;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
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
class DLCryptoDeserializeTest {

  @Mock private DLCryptoSession dlCryptoSession;

  private DLCryptoDeserialize deserializer;

  @BeforeEach
  void setUp() throws Exception {
    deserializer = new DLCryptoDeserialize();
    Field field = DLCryptoDeserialize.class.getDeclaredField("dlCryptoSession");
    field.setAccessible(true);
    field.set(deserializer, dlCryptoSession);
  }

  @Test
  void deserialize_shouldReturnNullValueAsIs() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn(null);

    String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertNull(result);
  }

  @Test
  void deserialize_shouldReturnEmptyValueAsIs() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("");

    String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertEquals("", result);
  }

  @Test
  void deserialize_shouldDecryptUsingEncryptedObjectFromConfig() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("encrypted_data");

    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    when(mockConfig.getEncryptedObject()).thenReturn("enc_obj");

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(mockDlCrypto.decrypt(anyString())).thenReturn("decrypted_data");

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class);
        MockedStatic<DLBCryptoLoader> mockedLoader = mockStatic(DLBCryptoLoader.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(true);
      mockedConfig.when(() -> AuthenticatorSSOConfig.getInstance()).thenReturn(mockConfig);
      mockedLoader.when(() -> DLBCryptoLoader.deserialize(anyString())).thenReturn(mockDlCrypto);

      String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

      assertEquals("decrypted_data", result);
      verify(mockDlCrypto).setClient(true);
    }
  }

  @Test
  void deserialize_shouldDecryptUsingSessionWhenNoConfigInstance() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("encrypted_data");

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(dlCryptoSession.getSession()).thenReturn(mockDlCrypto);
    when(mockDlCrypto.decrypt(anyString())).thenReturn("decrypted_data");

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

      assertEquals("decrypted_data", result);
      verify(mockDlCrypto).setClient(true);
    }
  }

  @Test
  void deserialize_shouldReturnOriginalValueWhenBothEncryptionSourcesAreNull() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("some_value");

    Field field = DLCryptoDeserialize.class.getDeclaredField("dlCryptoSession");
    field.setAccessible(true);
    field.set(deserializer, null);

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

      assertEquals("some_value", result);
    }
  }

  @Test
  void deserialize_shouldReturnOriginalValueWhenConfigInstanceHasNullEncryptedObjectAndNoSession()
      throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("some_value");

    AuthenticatorSSOConfig mockConfig = mock(AuthenticatorSSOConfig.class);
    when(mockConfig.getEncryptedObject()).thenReturn(null);

    Field field = DLCryptoDeserialize.class.getDeclaredField("dlCryptoSession");
    field.setAccessible(true);
    field.set(deserializer, null);

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(true);
      mockedConfig.when(() -> AuthenticatorSSOConfig.getInstance()).thenReturn(mockConfig);

      String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

      assertEquals("some_value", result);
    }
  }

  @Test
  void deserialize_shouldReturnOriginalValueWhenDlCryptoIsNull() throws Exception {
    JsonParser parser = mock(JsonParser.class);
    when(parser.getValueAsString()).thenReturn("some_value");
    when(dlCryptoSession.getSession()).thenReturn(null);

    try (MockedStatic<AuthenticatorSSOConfig> mockedConfig = mockStatic(AuthenticatorSSOConfig.class)) {
      mockedConfig.when(AuthenticatorSSOConfig::isExistSingletonInstance).thenReturn(false);

      String result = deserializer.deserialize(parser, mock(DeserializationContext.class));

      assertEquals("some_value", result);
    }
  }
}
