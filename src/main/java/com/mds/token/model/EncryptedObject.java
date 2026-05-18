package com.mds.token.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Model representing a serialised DLB encrypted object exchanged between
 * client and server via HTTP headers or authentication config.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class EncryptedObject extends AuthBase {

  public EncryptedObject(String value) {
    super(value);
  }

}
