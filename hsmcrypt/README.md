# HsmCrypt

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

HSM-based encryption/decryption library for Spring Boot with automatic property decryption

## Introduction

HsmCrypt provides HSM (Hardware Security Module) based encryption/decryption capabilities through the SIPWON library integration. It offers seamless Spring Boot integration with automatic property decryption and a CLI tool for encryption operations.

### Key Features

- **HSM-based Security**: Hardware-level encryption using HSM modules
- **AES Encryption**: AES algorithm with non-deterministic encryption (8-byte random prefix)
- **Auto Property Decryption**: Automatically decrypts `HCENC(...)` values in application properties
- **CLI Tool**: Command-line interface for encryption and verification (enc/vrf commands)
- **Spring Boot Auto-Configuration**: Easy integration with Spring Boot
- **Package-private API**: Internal implementation hidden from external users
- **ISO/IEC 9797-1 Padding**: Standard padding method for secure encryption

### Design Philosophy

HsmCrypt focuses on **property encryption** rather than general-purpose encryption. The package-private API design ensures:

- External users cannot directly call encryption methods
- Recommended usage: CLI tool for encryption + auto property decryption
- Internal implementation details remain hidden
- Only configuration and exception classes are public

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.prometheuskr</groupId>
    <artifactId>hsmcrypt</artifactId>
    <version>1.21.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.prometheuskr:hsmcrypt:1.21.0'
```

## Quick Start

### 1. Configuration

**application.yml**

```yaml
# SIPWON HSM Configuration (provided by sipwon-spring-boot-starter)
sipwon:
  # PKCS#11 library path
  pkcs11-library-path: /path/to/pkcs11/library.so
  
  # HSM token configuration
  tokens:
    - label: "HSM_TOKEN_1"
      pin: "1234"
  
  # Key caching (optional)
  use-cache-key: false

# HsmCrypt Configuration
hsmcrypt:
  encryption:
    enabled: true
    token-label: "HSM_TOKEN_1"
    key-label: "MY_ENCRYPTION_KEY"
```

**application.properties**

```properties
# SIPWON HSM Configuration
sipwon.pkcs11-library-path=/path/to/pkcs11/library.so
sipwon.tokens[0].label=HSM_TOKEN_1
sipwon.tokens[0].pin=1234

# HsmCrypt Configuration
hsmcrypt.encryption.enabled=true
hsmcrypt.encryption.token-label=HSM_TOKEN_1
hsmcrypt.encryption.key-label=MY_ENCRYPTION_KEY
```

### 2. Encrypt Values Using CLI

```bash
# Encrypt a value
java -jar hsmcrypt-1.21.0-exec.jar enc "MySecretPassword"
# Output: HCENC(3f8a7b2c1d9e4f5a...)

# Verify encryption
java -jar hsmcrypt-1.21.0-exec.jar vrf "MySecretPassword:HCENC(3f8a7b2c...)"
# Output: Valid
```

### 3. Use Encrypted Values in Properties

Encrypted values are automatically decrypted by Spring Boot:

```yaml
database:
  username: admin
  password: HCENC(3f8a7b2c1d9e4f5a6b7c8d9e0f1a2b3c)

api:
  key: HCENC(1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d)
```

```java
@Component
public class DatabaseConfig {
    
    @Value("${database.password}")  // Automatically decrypted
    private String password;
    
    @Value("${api.key}")  // Automatically decrypted
    private String apiKey;
}
```

## API Design

### Package-private Architecture

HsmCrypt uses package-private visibility to hide internal implementation:

**Package-private (Internal use only):**
- `HsmCrypt` - Core encryption/decryption implementation
- `HsmCryptHelper` - HCENC() format wrapper
- `StringEncryptor` - Minimal decryption interface
- `EncryptablePropertyResolver` - Auto property decryption

**Public API (External use):**
- `HsmCryptAutoConfiguration` - Spring Boot auto-configuration
- `HsmCryptProperties` - Configuration properties
- `HsmCryptException` - Exception class
- `HsmCryptCli` - CLI tool entry point

### Why Package-private?

External users cannot directly access encryption methods. This design enforces the recommended usage pattern:

1. **Encryption**: Use CLI tool (`enc` command)
2. **Decryption**: Automatic via property resolver
3. **Verification**: Use CLI tool (`vrf` command)

If you need programmatic access to encryption methods, create your own Spring Boot Starter that depends on hsmcrypt and exposes a controlled API.

## Encryption Features

### Algorithm
- **AES only**: CBC mode
- **Non-deterministic**: 8-byte random prefix per encryption
- **Padding**: ISO/IEC 9797-1 Method 2
- **Format**: `HCENC(hexencodedvalue)` - hex encoding

### Security Properties
- Each encryption produces different output (random prefix)
- Same plaintext encrypted twice yields different ciphertext
- Provides semantic security for property encryption

## CLI Tool Usage

HsmCrypt provides a CLI tool for encryption and verification operations.

### Build

```bash
cd hsmcrypt
mvn clean package
```

This generates `target/hsmcrypt-1.21.0-exec.jar` (executable JAR with dependencies).

### Commands

The CLI provides two commands:

#### enc - Encrypt a value

```bash
java -jar hsmcrypt-1.21.0-exec.jar enc "plaintext"
```

Output:
```
HCENC(3f8a7b2c1d9e4f5a...)
```

#### vrf - Verify encryption

Verifies that encrypted value matches plaintext:

```bash
java -jar hsmcrypt-1.21.0-exec.jar vrf "plaintext:HCENC(3f8a7b2c...)"
```

Output:
```
Valid

or

Invalid
```

### Configuration

The CLI requires `application.yml` in the current directory. If not found, it automatically creates a template:

```yaml
sipwon:
  pkcs11-library-path: /path/to/cryptoki.dll
  tokens:
    - label: "TOKEN_LABEL"
      pin: "1234"

hsmcrypt:
  encryption:
    enabled: true
    token-label: "TOKEN_LABEL"
    key-label: "KEY_LABEL"
```

### Batch Processing Example

**Linux/macOS:**
```bash
#!/bin/bash
encrypt() {
  java -jar hsmcrypt-1.21.0-exec.jar enc "$1"
}

DB_PASSWORD=$(encrypt "MySecretPassword")
echo "Encrypted: $DB_PASSWORD"
```

**Windows PowerShell:**
```powershell
function Encrypt-Value {
    param([string]$value)
    java -jar hsmcrypt-1.21.0-exec.jar enc $value
}

$dbPassword = Encrypt-Value "MySecretPassword"
Write-Host "Encrypted: $dbPassword"
```

## Configuration Options

### SIPWON Configuration

| Property | Description | Required |
|----------|-------------|----------|
| `sipwon.pkcs11-library-path` | Path to PKCS#11 library | ✅ |
| `sipwon.tokens[].label` | HSM token label | ✅ |
| `sipwon.tokens[].pin` | HSM token PIN | ✅ |

### HsmCrypt Configuration

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `hsmcrypt.encryption.enabled` | Enable encryption feature | false | ✅ |
| `hsmcrypt.encryption.token-label` | Token label for encryption | - | ✅ |
| `hsmcrypt.encryption.key-label` | Key label for encryption | - | ✅ |

## Troubleshooting

### HSM Connection Failure

```
Failed to initialize HSM module configuration
```

**Solutions:**
- Verify PKCS#11 library path is correct
- Check HSM token connection status
- Verify token PIN
- Check library file permissions

### Key Not Found

```
Failed to find HSM key
```

**Solutions:**
- Verify key exists in HSM
- Check key label is correct
- Ensure key type matches (AES)

### Decryption Failure

```
Failed to decrypt data
```

**Solutions:**
- Verify same key used for encryption and decryption
- Check data format (must be HCENC(hexvalue))
- Verify data is not corrupted

## Security Considerations

1. **Key Management**: Keys managed inside HSM, minimizing external exposure
2. **PIN Protection**: Store HSM PIN in environment variables or external configuration
3. **Logging**: Ensure encrypted values and key information are not logged

## Example Project

See the [hsmcrypt-example](../hsmcrypt-example) project for a complete working example with:

- Spring Boot integration
- Auto property decryption
- Configuration examples

## Documentation

- [EXAMPLES.md](EXAMPLES.md) - Detailed usage examples
- [API Javadoc](https://javadoc.io/doc/io.github.prometheuskr/hsmcrypt) - API documentation

## License

Apache License 2.0

## Contributing

Issues and Pull Requests are welcome!

## Links

- [SIPWON GitHub](https://github.com/prometheus-kr/sipwon)
- [Contact](mailto:prometheus@kakao.com)
