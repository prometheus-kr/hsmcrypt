# HsmCrypt Project

Java-based HSM (Hardware Security Module) encryption/decryption library

## Project Information

- **Language**: Java 21
- **Build Tool**: Maven
- **Framework**: Spring Boot 3.5.9
- **Package**: io.github.prometheuskr.hsmcrypt
- **Version**: 1.21.0

## Project Structure

- Core library: `hsmcrypt/` (Maven project)
- Package-private classes: HsmCrypt, HsmCryptHelper, EncryptablePropertyResolver
- Public classes: HsmCryptAutoConfiguration, HsmCryptProperties, HsmCryptException, HsmCryptCli
- Spring Boot auto-configuration support for property decryption
- Example project: `hsmcrypt-example/` (demonstrates usage)

## Build Commands

- Build all: `.\build.ps1`
- Compile: `mvn clean compile`
- Test: `mvn test`
- Package: `mvn package`
- Install to local repo: `mvn install`
- Generate javadoc: `mvn javadoc:javadoc`

## Key Features

- **AES Encryption Only**: Simplified to support only AES algorithm
- **Non-deterministic Encryption**: 8-byte random prefix for each encryption
- **ISO/IEC 9797-1 Padding**: Standard padding method
- **HCENC() Format**: Encrypted properties format `HCENC(hexencodedvalue)`
- **Auto Property Decryption**: Spring Boot integration automatically decrypts HCENC() values in application properties
- **CLI Tool**: Command-line interface for encryption and verification
- **Package-private API**: Internal implementation hidden, only configuration and CLI exposed

## Architecture

- All core classes in single package (io.github.prometheuskr.hsmcrypt) for package-private access control
- EncryptablePropertyResolver: BeanFactoryPostProcessor for automatic property decryption
- Lazy initialization pattern for HsmCryptHelper to avoid circular dependencies
- HSM integration via SIPWON library
- Spring Boot 3.x: Uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for auto-configuration (not spring.factories)
