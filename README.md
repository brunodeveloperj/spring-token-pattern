# spring-token-pattern

Biblioteca Spring Boot reutilizável que centraliza a obtenção, renovação automática e propagação de tokens OAuth 2.0 via SSO, além de fornecer serialização/deserialização criptográfica transparente em DTOs Jackson.

## Dependência

```xml
<dependency>
  <groupId>com.mds</groupId>
  <artifactId>spring-token-pattern</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## Funcionalidades

- **Obtenção de token SSO** — client credentials grant via Feign Client com form-encoded
- **Singleton com renovação automática** — `AuthenticatorSSOConfig` gerencia ciclo de vida do token com expiração, subtraction factor (15s) e renovação assíncrona
- **Encrypted object management** — criação e renovação do objeto criptográfico DLB integrado ao ciclo do token
- **Duas estratégias de autenticação**:
  - `"manager"` — obtém token e encrypted object do singleton SSO
  - `"servlet"` — extrai token e encrypted object dos headers HTTP da requisição atual
- **Jackson Serialize/Deserialize** — `DLCryptoSerialize` e `DLCryptoDeserialize` para criptografia/descriptografia transparente em campos de DTOs
- **Servlet support** — `HttpServletService` + `RequestContextListener` para acesso request-scoped

---

## Configuração

```yaml
authentication:
  config:
    initConstructionOfSsoConfig: true   # inicializa singleton no startup
    headerNameToken: Authorization
    headerNameEncryptedObject: X-Encrypted-Object
  sso:
    client:
      accessTokenUrl: https://sso-server/auth/realms/myrealm/protocol/openid-connect/token
      clientId: my-client
      clientSecret: ${SSO_CLIENT_SECRET}
      grantType: client_credentials
```

---

## Exemplo de uso

### Via Manager (backend-to-backend)

```java
@Autowired
@Qualifier("manager")
private AuthenticationService authService;

Token token = authService.getToken();
EncryptedObject encObj = authService.getEncryptedObject();

// Usar token.getValue() como header Authorization
```

### Via Servlet (frontend-to-backend)

```java
@Autowired
@Qualifier("servlet")
private AuthenticationService authService;

Token token = authService.getToken();        // do header HTTP
EncryptedObject encObj = authService.getEncryptedObject();
```

### Jackson Crypto (em DTOs)

```java
@JsonDeserialize(using = DLCryptoDeserialize.class)
private String sensitiveField;

@JsonSerialize(using = DLCryptoSerialize.class)
private String sensitiveOutput;
```

---

## Arquitetura

```
Application
    ↓
AuthenticationService ("manager" ou "servlet")
    ↓ (manager)                    ↓ (servlet)
AuthenticatorSSOConfig          HttpServletService
    ↓                                ↓
SSOFeignClient (Feign)          HttpServletRequest headers
    ↓
SSO / Keycloak (OAuth 2.0)
```

---

## Estrutura do projeto

```
src/main/java/com/mds/token/
├── TokenAutoConfig.java                     # Auto-config + component scan
├── config/
│   └── AuthenticationPropertiesConfig.java  # Properties (authentication.config.*)
├── crypto/
│   ├── DLCryptoDeserialize.java            # Jackson deserializer com decrypt
│   └── DLCryptoSerialize.java             # Jackson serializer com encrypt
├── keys/
│   ├── MessagesKeys.java                  # Códigos ARCAUT_0001, ARCAUT_0002
│   └── SSOKeys.java                    # Nomes de parâmetros OAuth
├── model/
│   ├── AuthBase.java                      # Base com campo value
│   ├── EncryptedObject.java              # Wrapper para encrypted object
│   └── Token.java                        # Wrapper para bearer token
├── sso/
│   ├── config/
│   │   ├── AuthenticatorSSOConfig.java  # Singleton com auto-renewal
│   │   ├── CoreFeignConfig.java          # FormEncoder para Feign
│   │   └── SSOConfig.java             # Properties (authentication.sso.client.*)
│   ├── dto/
│   │   └── SSOResponseDTO.java        # Response OAuth 2.0
│   ├── feign/client/
│   │   ├── SSOFeignClient.java        # Feign interface
│   │   └── service/
│   │       ├── SSOService.java        # Interface
│   │       └── impl/
│   │           └── SSOServiceImpl.java # Implementação
│   └── handler/
│       └── AuthenticationSSOHandler.java # PostConstruct + build
├── service/
│   ├── AuthenticationService.java        # Interface (getToken + getEncryptedObject)
│   └── impl/
│       ├── AuthenticationManagerServiceImpl.java  # "manager"
│       └── AuthenticationServletServiceImpl.java  # "servlet"
└── servlet/
    ├── config/
    │   └── RequestContextListenerConfig.java  # ConditionalOnWebApplication
    └── service/
        ├── HttpServletService.java            # Interface
        └── impl/
            └── HttpServletServiceImpl.java    # ObjectProvider-based
```

---

## Requisitos

- **Java** 21+
- **Spring Boot** 4.x
- **Spring Cloud** 2025.1.1+ (OpenFeign)
- **spring-crypto-pattern** (dependência para DLCrypto e EncryptedObjectHandler)
- **spring-error-pattern** (dependência para GeneralException e ErrorUtils)
- **shared-core-lib** (dependência para ObjectUtils e FunctionUtils)

---

## Kill-switch

Para desabilitar completamente a auto-configuração:

```yaml
authentication:
  enabled: false
```

Por padrão (`matchIfMissing = true`), a lib é ativada automaticamente.

---

## Melhorias futuras

- Suporte a refresh token grant
- Cache distribuído de tokens entre instâncias
- Suporte multi-tenant (múltiplos realms)
- Métricas Micrometer para latência de token requests
