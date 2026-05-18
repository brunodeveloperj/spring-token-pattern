package com.mds.token.sso.config;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.mds.crypto.v1.handler.EncryptedObjectHandler;
import com.mds.token.sso.dto.SSOResponseDTO;
import com.mds.token.sso.feign.client.service.SSOService;
import com.mds.error.handler.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;

/**
 * A class that manages the authentication process.
 * <p>
 * This class is responsible for obtaining and managing the authentication token and encrypted object.
 * It also provides a method to check if the authentication token has expired.
 * <p>
 * To use this class, you must first create a new instance of it.
 * You can do this by calling the `getInstance()` method.
 * This method will return the singleton instance of the class.
 * <p>
 * Once you have an instance of the class, you can call the `isTokenExpire()` method to check if the authentication token has expired.
 * If the token has expired, you should call the `getInstance()` method again to obtain a new token.
 * <p>
 * You can also use the class to get the encrypted object.
 * To do this, you can call the `getEncryptedObject()` method.
 * This method will return the encrypted object, or `null` if the encrypted object is not available.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Getter @Setter
public final class AuthenticatorSSOConfig {

  /**
   * The field must be declared volatile so that double check lock would work
   * correctly.
   */
  private static volatile AuthenticatorSSOConfig instance;

  private static final AtomicBoolean runTokenAsync = new AtomicBoolean(false);
  private static final long EXPIRATION_SUBTRACTION_FACTOR = 15;
  private static final long MAX_DIFFERENCE_BY_ENCRYPTED_OBJECT = 1;
  private static ExecutorService executor;

  static {
    executor = createExecutor();
    Runtime.getRuntime().addShutdownHook(new Thread(AuthenticatorSSOConfig::shutdownExecutor));
  }

  private String authorization;
  private String refreshToken;
  private String encryptedObject;
  private Integer maxDifferenceByToken;
  private Integer maxDifferenceByRefreshToken;
  private LocalDateTime tokenExpiration;
  private LocalDateTime encryptedObjectExpiration;

  private final SSOService ssoClient;
  private final EncryptedObjectHandler encryptedObjectHandler;

  /**
   * Constructor for the AuthenticatorManagerConfig class.
   * <p>
   * This constructor is responsible for obtaining the authentication
   * token and encrypted object from the SSO client and encrypted object handler, respectively.
   * It also sets the token expiration and encrypted object expiration times.
   *
   * @param ssoClient The SSO client to use.
   * @param encryptedObjectHandler The encrypted object handler to use.
   * @throws GeneralException If an error occurs while obtaining the authentication token or encrypted object.
   */
  AuthenticatorSSOConfig(final SSOService ssoClient, final EncryptedObjectHandler encryptedObjectHandler) throws GeneralException {
    this.ssoClient = ssoClient;
    this.encryptedObjectHandler = encryptedObjectHandler;
    final SSOResponseDTO ssoToken = this.ssoClient.token();
    this.tokenExpiration = now();

    this.authorization = ssoToken.getAccessToken();
    this.refreshToken = ssoToken.getRefreshToken();
    this.maxDifferenceByToken = ssoToken.getExpiresIn();
    this.maxDifferenceByRefreshToken = ssoToken.getRefreshExpiresIn();
    this.encryptedObjectExpiration = now();
    this.encryptedObject = this.encryptedObjectHandler.create();
  }

  /**
   * Gets the singleton instance of the AuthenticatorManagerConfig.
   * <p>
   * This method uses the double-checked locking pattern to ensure that only one instance of the AuthenticatorManagerConfig class is created.
   * <p>
   * This way we will have a unique life cycle for the thread responsible for the AuthenticatorManager instance.
   *
   * @param ssoClient the SSO client to use
   * @param encryptedObjectHandler the encrypted object handler to use
   * @return the singleton instance of the AuthenticatorManagerConfig class
   * @throws GeneralException if an error occurs while getting the instance
   */
  public static AuthenticatorSSOConfig build(SSOService ssoClient, EncryptedObjectHandler encryptedObjectHandler) throws GeneralException {
    synchronized (AuthenticatorSSOConfig.class) {
      if (instance == null) {
        instance = new AuthenticatorSSOConfig(ssoClient, encryptedObjectHandler);
      }
      return instance;
    }
  }

  /**
   * Shuts down the internal executor service, releasing thread resources.
   */
  private static void shutdownExecutor() {
    ExecutorService localExecutor = executor;
    if (localExecutor != null) {
      localExecutor.shutdown();
      try {
        if (!localExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          localExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        localExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private static ExecutorService createExecutor() {
    return Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    });
  }

  /**
   * Get instance value of AuthenticatorManagerConfig singleton class.
   *
   * @return instance singleton class.
   */
  public static AuthenticatorSSOConfig getInstance() throws GeneralException {
    AuthenticatorSSOConfig config = instance;
    if (config != null) {
      synchronized (AuthenticatorSSOConfig.class) {
        checkManagerConfig(config);
      }
    }
    return config;
  }

  public static boolean isExistSingletonInstance() {
    return (instance != null);
  }

  /**
   * Checks if the token has expired.
   *
   * @return `true` if the authentication token has expired, `false` otherwise.
   */
  public boolean isTokenExpire() {
    return (checkCurrentDifferenceForTokenExpiration() > this.getMaxDifferenceByToken());
  }

  /**
   * Checks if the AuthenticatorManagerConfig is expired and updates it if necessary.
   * <p>
   * This method checks if the authentication token and encrypted object in the AuthenticatorManagerConfig are expired.
   * If either the token or encrypted object is expired, the method updates the AuthenticatorManagerConfig with a new token and/or encrypted object.
   * <p>
   * Additionally, this method validates the following:
   * * If the token is expired or about to expire (based on the `tokenExpireBySubtractionFactor` property), the method generates a new token asynchronously.
   * * If the encrypted object is expired, the method generates a new encrypted object.
   *
   * @param config The AuthenticatorManagerConfig to check.
   * @throws GeneralException If an error occurs while checking or updating the AuthenticatorManagerConfig.
   */
  private static void checkManagerConfig(AuthenticatorSSOConfig config) throws GeneralException {

    // Checking if the token value is expired.
    if (config.isTokenExpire()) {

      // Generate token
      config.generateTokenSSO();
    } else if(config.isTokenExpireBySubtractionFactor()) {

      if (runTokenAsync.compareAndSet(false, true)) {
        // Generate token asynchronously
        generateTokenSSOAsync();
      }
    }

    // Checking if the encryptedObject value is expired.
    if(config.isEncryptedObjectExpire()) {

      // generating a new encryptedObject value.
      config.setEncryptedObject(config.getEncryptedObjectHandler().create());
      config.setEncryptedObjectExpiration(now());
    }
  }

  /**
   * Checks if the token has expired, subtracting 15 seconds to ensure that a new token is generated
   * before the previous one expires.
   *
   * @return `true` if the authentication token has expired according to the subtraction factor,
   * `false` otherwise.
   */
  private boolean isTokenExpireBySubtractionFactor() {
    final long expirationThreshold =
        Math.max(0L, this.getMaxDifferenceByToken() - EXPIRATION_SUBTRACTION_FACTOR);
    return (checkCurrentDifferenceForTokenExpiration() > expirationThreshold);
  }

  /**
   * Calculates the difference between the current time and the token expiration time in seconds.
   *
   * @return The difference between the current time and the token expiration time in seconds.
   */
  private long checkCurrentDifferenceForTokenExpiration() {
    return SECONDS.between(this.getTokenExpiration(), now());
  }

  /**
   * Gets the encrypted object.
   *
   * @return The encrypted object, or `null` if the encrypted object is not available.
   */
  private boolean isEncryptedObjectExpire() {
    return (this.getEncryptedObject() == null)
        || (this.getEncryptedObjectExpiration() == null)
        || (DAYS.between(this.getEncryptedObjectExpiration(), now()) > MAX_DIFFERENCE_BY_ENCRYPTED_OBJECT);
  }

  /**
   * Generates a new token from the SSO server.
   * <p>
   * This method sets the token expiration time, authorization header, refresh token, and the maximum difference between the current time and the token expiration time.
   *
   * @throws GeneralException if an error occurs while generating the token
   */
  private void generateTokenSSO() throws GeneralException {
    // Get a new token from the SSO server.
    final SSOResponseDTO token = getSsoClient().token();

    // Set the token expiration time only after successful fetch.
    this.setTokenExpiration(now());

    // Set the authorization header.
    this.setAuthorization(token.getAccessToken());

    // Set the maximum difference between the current time and the token expiration time.
    this.setMaxDifferenceByToken(token.getExpiresIn());

    // Set the refresh token.
    this.setRefreshToken(token.getRefreshToken());

    // Set the maximum difference between the current time and the refresh token expiration time.
    this.setMaxDifferenceByRefreshToken(token.getRefreshExpiresIn());
  }

  /**
   * Generates an SSO token asynchronously.
   *
   * @throws CompletionException If an error occurs while generating the token.
   */
  private static void generateTokenSSOAsync() {
    executor.submit(() -> {
      try {
        if (instance != null) {
          instance.generateTokenSSO();
        }
      } catch (GeneralException ex) {
        throw new CompletionException("Error generating token.", ex);
      } finally {
        runTokenAsync.set(false);
      }
    });
  }

}
