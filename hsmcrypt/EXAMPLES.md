# HsmCrypt Usage Examples

## Basic Usage

### 1. CLI Tool for Encryption

The library includes a CLI tool for encrypting values and verifying encrypted values.

**Encrypt a value:**
```bash
java -jar hsmcrypt-1.8.0-exec.jar enc "password123"
```

**Verify encryption:**
```bash
java -jar hsmcrypt-1.8.0-exec.jar vrf "password123:HCENC(3f8a7b2c...)"
```

Note: The CLI tool requires `application.yml` with HSM configuration in the current directory.

### 2. Auto Property Decryption

HsmCrypt automatically decrypts properties with `HCENC()` format in Spring Boot applications.

**application.yml:**
```yaml
# SIPWON HSM Configuration
sipwon:
  pkcs11-library-path: /usr/local/lib/softhsm/libsofthsm2.so
  tokens:
    - label: "HSM_TOKEN_1"
      pin: "1234"

# HsmCrypt Configuration
hsmcrypt:
  encryption:
    enabled: true
    token-label: "HSM_TOKEN_1"
    key-label: "MY_KEY"

# Application Properties (HCENC values are automatically decrypted)
myapp:
  database:
    password: HCENC(3f8a7b2c1d9e4f5a6b7c8d9e0f1a2b3c)
  api:
    key: HCENC(1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d)
```

**Java code:**
```java
@Configuration
public class DatabaseConfig {
    
    @Value("${myapp.database.password}")
    private String password;  // Automatically decrypted
    
    @Value("${myapp.api.key}")
    private String apiKey;    // Automatically decrypted
    
    @Bean
    public DataSource dataSource() {
        // password is already decrypted
        return DataSourceBuilder.create()
            .password(password)
            .build();
    }
}
```

## Important Notes

### API Visibility

HsmCrypt uses package-private API design to hide internal implementation:

- **Package-private (Internal)**: `HsmCrypt`, `HsmCryptHelper`, `StringEncryptor`, `EncryptablePropertyResolver`
- **Public (External)**: `HsmCryptAutoConfiguration`, `HsmCryptProperties`, `HsmCryptException`, `HsmCryptCli`

External users cannot directly access encryption/decryption methods. Use one of these approaches:

1. **Recommended**: Use CLI tool for encryption and auto property decryption feature
2. Build your own Spring Boot Starter that depends on hsmcrypt and exposes controlled API

### Encryption Features

- **Algorithm**: AES only (CBC mode)
- **Non-deterministic**: Each encryption produces different output (8-byte random prefix)
- **Padding**: ISO/IEC 9797-1 Method 2
- **Format**: `HCENC(hexencodedvalue)` where value is hex-encoded (not base64)

### CLI Usage

The CLI tool (`hsmcrypt-1.8.0-exec.jar`) provides two commands:

**enc** - Encrypt a value:
```bash
java -jar hsmcrypt-1.8.0-exec.jar enc "plaintext"
# Output: HCENC(3f8a7b2c...)
```

**vrf** - Verify encryption (format: `plaintext:encrypted`):
```bash
java -jar hsmcrypt-1.8.0-exec.jar vrf "plaintext:HCENC(3f8a7b2c...)"
# Output: Verification successful / Verification failed
```

The CLI automatically creates `application.yml` template if not found.

## Advanced Configuration

### Custom Prefix/Suffix Format

While `HsmCryptHelper` is package-private, you can configure custom format via auto-configuration:

```yaml
hsmcrypt:
  encryption:
    enabled: true
    token-label: "HSM_TOKEN_1"
    key-label: "MY_KEY"
    prefix: "HSM["
    suffix: "]"
```

This will change the format from `HCENC(...)` to `HSM[...]`.

## Real-world Scenarios

### Database Credentials

Store encrypted credentials in properties:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: dbuser
    password: HCENC(a1b2c3d4e5f6...)
```

The password is automatically decrypted when Spring Boot creates the DataSource.

### API Keys

```yaml
external:
  api:
    key: HCENC(1a2b3c4d...)
    secret: HCENC(9f8e7d6c...)
```

```java
@Component
public class ExternalApiClient {
    
    @Value("${external.api.key}")
    private String apiKey;  // Decrypted
    
    @Value("${external.api.secret}")
    private String apiSecret;  // Decrypted
    
    public void callApi() {
        // Use decrypted values
    }
}
```

### Multiple Environments

Use different encrypted values per environment:

**application-dev.yml:**
```yaml
myapp:
  secret: HCENC(dev123...)
```

**application-prod.yml:**
```yaml
myapp:
  secret: HCENC(prod456...)
```

Each environment's values are encrypted with appropriate HSM keys.
