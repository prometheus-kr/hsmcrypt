# HsmCrypt Project

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.prometheus-kr/hsmcrypt.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.prometheus-kr/hsmcrypt)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)

HSM-based encryption/decryption library for Spring Boot with automatic property decryption

## Introduction

HsmCrypt provides HSM (Hardware Security Module) based encryption/decryption capabilities through the SIPWON library integration. It offers seamless Spring Boot integration with automatic property decryption and a CLI tool for encryption operations.

## Key Features
- Hardware-level security (HSM)
- AES encryption only (CBC mode, ISO/IEC 9797-1 padding)
- Non-deterministic encryption (8-byte random prefix)
- HCENC(hexencodedvalue) format for encrypted properties
- Spring Boot auto-configuration for property decryption
- CLI tool: enc/vrf commands
- Package-private API (internal implementation hidden)

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.prometheuskr</groupId>
    <artifactId>hsmcrypt</artifactId>
    <version>1.8.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.prometheuskr:hsmcrypt:1.8.0'
```

## Quick Start

### Configuration
**application.yml**
```yaml
sipwon:
  pkcs11-library-path: /path/to/pkcs11/library.so
  tokens:
    - label: "HSM_TOKEN_1"
      pin: "1234"
  use-cache-key: false
hsmcrypt:
  encryption:
    enabled: true
    token-label: "HSM_TOKEN_1"
    key-label: "MY_ENCRYPTION_KEY"
```

**application.properties**
```properties
sipwon.pkcs11-library-path=/path/to/pkcs11/library.so
sipwon.tokens[0].label=HSM_TOKEN_1
sipwon.tokens[0].pin=1234
hsmcrypt.encryption.enabled=true
hsmcrypt.encryption.token-label=HSM_TOKEN_1
hsmcrypt.encryption.key-label=MY_ENCRYPTION_KEY
```

### CLI Usage
```bash
java -jar hsmcrypt-1.8.0-exec.jar enc "MySecretPassword"
# Output: HCENC(3f8a7b2c1d9e4f5a...)

java -jar hsmcrypt-1.8.0-exec.jar vrf "MySecretPassword:HCENC(3f8a7b2c...)"
# Output: Valid
# Output: Invalid
```

### Property Auto-Decryption
Encrypted values in properties are automatically decrypted:
```yaml
database:
  password: HCENC(3f8a7b2c...)
api:
  key: HCENC(1a2b3c4d...)
```

## Example Project
See [hsmcrypt-example](hsmcrypt-example/README.md) for a full Spring Boot usage example.

## API Design
- Internal classes: HsmCrypt, HsmCryptHelper, StringEncryptor, EncryptablePropertyResolver (package-private)
- Public API: HsmCryptAutoConfiguration, HsmCryptProperties, HsmCryptException, HsmCryptCli

## Troubleshooting
- HSM connection/configuration errors: check PKCS#11 path, token label, PIN
- Key not found: verify key exists in HSM
- Decryption errors: check key, format, and data integrity

## License
Apache License 2.0

## Links
- [GitHub Repository](https://github.com/prometheus-kr/hsmcrypt)
- [SIPWON GitHub](https://github.com/prometheus-kr/sipwon)
- [Contact](mailto:prometheus@kakao.com)
