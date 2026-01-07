# HsmCrypt Example Project

This project demonstrates how to use the HsmCrypt library in a Spring Boot application.

## Project Structure

```
hsmcrypt-example/
├── src/
│   └── main/
│       └── java/io/github/prometheuskr/hsmcrypt/example/
│           └── HsmCryptExampleApplication.java    # Main Application
└── pom.xml
```

## Quick Start

### Build & Run (Recommended)

```powershell
# Full build (library install + project build)
.\build.ps1 full
.\run.ps1
```

Or with Maven:
```bash
cd ../hsmcrypt
mvn clean install
cd ../hsmcrypt-example
mvn clean package
java -jar target/hsmcrypt-example-1.0.0.jar
```

## REST API Usage

- Health check: `GET /api/crypto/health`
- Encrypt: `POST /api/crypto/encrypt` (body: `{ "text": "Hello, World!" }`)
- Decrypt: `POST /api/crypto/decrypt` (body: `{ "text": "HCENC(...)" }`)
- Properties: `GET /api/crypto/properties`

## Example Code

```java
@Autowired
private EncryptionService encryptionService;

String encrypted = encryptionService.encrypt("sensitive data");
String decrypted = encryptionService.decrypt(encrypted);
```

## Property Encryption

Encrypted values in properties (application.properties):
```properties
app.db.password=HCENC(encryptedValue)
```
Injected and auto-decrypted:
```java
@Value("${app.db.password}")
private String dbPassword;
```

## Configuration

```properties
sipwon.pkcs11-library-path=/usr/local/lib/softhsm/libsofthsm2.so
sipwon.tokens[0].label=HSM_TOKEN_1
sipwon.tokens[0].pin=1234
hsmcrypt.encryption.enabled=true
hsmcrypt.encryption.token-label=HSM_TOKEN_1
hsmcrypt.encryption.key-label=MY_ENCRYPTION_KEY
```

## License
Apache License 2.0

## Reference
- [HsmCrypt root README](../README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
