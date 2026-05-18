package com.mds.token.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base model holding a single string value used as the foundation for
 * authentication artefacts such as {@link Token} and {@link EncryptedObject}.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthBase {

  private String value;

}
