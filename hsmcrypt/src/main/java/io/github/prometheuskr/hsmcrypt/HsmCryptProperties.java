package io.github.prometheuskr.hsmcrypt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration properties for HsmCrypt.
 * <p>
 * Binds properties with the prefix <code>hsmcrypt</code> from the application's
 * configuration files.
 * <p>
 * Note: HSM connection settings (pkcs11-library-path, tokens) are configured
 * via sipwon-spring-boot-starter with the 'sipwon' prefix.
 * 
 * @author Prometheus
 */
@Data
@ConfigurationProperties(prefix = "hsmcrypt")
public class HsmCryptProperties {

    /**
     * Default constructor.
     */
    public HsmCryptProperties() {
    }

    /**
     * Default key label for HSM encryption key.
     */
    public static final String DEFAULT_KEY_LABEL = "HsmCryptKey";

    /**
     * Default prefix for encrypted values.
     */
    public static final String DEFAULT_PREFIX = "HCENC(";

    /**
     * Default suffix for encrypted values.
     */
    public static final String DEFAULT_SUFFIX = ")";

    /**
     * Encryption configuration.
     */
    private Encryption encryption = new Encryption();

    /**
     * Encryption configuration properties.
     */
    @Data
    public static class Encryption {
        /**
         * Default constructor.
         */
        public Encryption() {
        }

        /**
         * Flag to enable or disable encryption functionality.
         */
        private Boolean enabled = Boolean.FALSE;

        /**
         * The token label to use for encryption operations.
         */
        private String tokenLabel;

        /**
         * The key label to use for encryption/decryption operations.
         * Defaults to {@value HsmCryptProperties#DEFAULT_KEY_LABEL}.
         */
        private String keyLabel = DEFAULT_KEY_LABEL;
    }
}
