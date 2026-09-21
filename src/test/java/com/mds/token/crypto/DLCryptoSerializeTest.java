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
import com.mds.token.sso.SsoSessionProvider;
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
  @Mock private SsoSessionProvider ssoSessionProvider;
  @Mock private JsonGenerator jsonGenerator;
  @Mock private SerializationContext serializationContext;

  private DLCryptoSerialize serializer;

  @BeforeEach
  void setUp() throws Exception {
    serializer = new DLCryptoSerialize();
    setField(serializer, "dlCryptoSession", dlCryptoSession);
    setField(serializer, "ssoSessionProvider", ssoSessionProvider);
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = DLCryptoSerialize.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
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
  void serialize_shouldEncryptValueUsingEncryptedObjectFromProvider() throws Exception {
    when(ssoSessionProvider.hasSession()).thenReturn(true);
    when(ssoSessionProvider.getEncryptedObject()).thenReturn("enc_obj");

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(mockDlCrypto.encrypt(anyString())).thenReturn("encrypted_result");

    try (MockedStatic<DLBCryptoLoader> mockedLoader = mockStatic(DLBCryptoLoader.class)) {
      mockedLoader.when(() -> DLBCryptoLoader.deserialize(anyString())).thenReturn(mockDlCrypto);

      serializer.serialize("plain_value", jsonGenerator, serializationContext);

      verify(mockDlCrypto).setClient(true);
      verify(jsonGenerator).writeString("encrypted_result");
    }
  }

  @Test
  void serialize_shouldEncryptValueUsingSessionWhenNoProviderSession() throws Exception {
    when(ssoSessionProvider.hasSession()).thenReturn(false);

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(dlCryptoSession.getSession()).thenReturn(mockDlCrypto);
    when(mockDlCrypto.encrypt(anyString())).thenReturn("encrypted_result");

    serializer.serialize("plain_value", jsonGenerator, serializationContext);

    verify(mockDlCrypto).setClient(true);
    verify(jsonGenerator).writeString("encrypted_result");
  }

  @Test
  void serialize_shouldThrowIOExceptionWhenBothEncryptionSourcesAreNull() throws Exception {
    when(ssoSessionProvider.hasSession()).thenReturn(false);
    setField(serializer, "dlCryptoSession", null);

    assertThrows(RuntimeException.class, () -> serializer.serialize("plain_value", jsonGenerator, serializationContext));
  }

  @Test
  void serialize_shouldThrowRuntimeExceptionOnEncryptionException() throws Exception {
    when(ssoSessionProvider.hasSession()).thenReturn(false);

    DLCrypto mockDlCrypto = mock(DLCrypto.class);
    when(dlCryptoSession.getSession()).thenReturn(mockDlCrypto);
    when(mockDlCrypto.encrypt(anyString())).thenThrow(new RuntimeException("Encryption failed"));

    assertThrows(RuntimeException.class, () -> serializer.serialize("plain_value", jsonGenerator, serializationContext));
  }
}
