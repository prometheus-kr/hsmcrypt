package io.github.prometheuskr.hsmcrypt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import iaik.pkcs.pkcs11.TokenException;
import io.github.prometheuskr.sipwon.autoconfig.HsmSessionFactoryRegistry;
import io.github.prometheuskr.sipwon.constant.HsmKeyType;
import io.github.prometheuskr.sipwon.constant.HsmMechanism.HsmCypherMode;
import io.github.prometheuskr.sipwon.session.HsmSession;
import io.github.prometheuskr.sipwon.session.HsmSessionFactory;

/**
 * Auto-configuration class for HsmCrypt.
 * <p>
 * This configuration class is activated automatically and provides beans for
 * HSM-based encryption similar to Jasypt functionality.
 * Uses sipwon-spring-boot-starter for HsmSessionFactoryRegistry auto-configuration.
 * 
 * @author Prometheus
 */
@Configuration
@EnableConfigurationProperties(HsmCryptProperties.class)
public class HsmCryptAutoConfiguration {

    /**
     * Default constructor.
     */
    public HsmCryptAutoConfiguration() {}

    /**
     * Creates an HsmCrypt bean.
     * <p>
     * This bean is only created when the 'hsmcrypt.encryption.enabled' property is
     * set to true.
     * HsmSessionFactoryRegistry is auto-configured by sipwon-spring-boot-starter.
     * 
     * @param hsmSessionFactoryRegistry
     *            the HSM session factory registry (provided by
     *            sipwon-spring-boot-starter)
     * @param properties
     *            the HsmCrypt properties
     * @return a configured HsmCrypt instance
     */
    @Bean
    @ConditionalOnMissingBean(HsmCrypt.class)
    @ConditionalOnProperty(prefix = "hsmcrypt.encryption", name = "enabled", havingValue = "true")
    public HsmCrypt hsmCrypt(HsmSessionFactoryRegistry hsmSessionFactoryRegistry, HsmCryptProperties properties) {
        HsmCryptProperties.Encryption encConfig = properties.getEncryption();

        // Ensure AES key exists in HSM
        ensureKeyExists(hsmSessionFactoryRegistry.getFactory(encConfig.getTokenLabel()), encConfig.getTokenLabel(),
                encConfig.getKeyLabel());

        return new HsmCrypt(
                hsmSessionFactoryRegistry.getFactory(encConfig.getTokenLabel()),
                encConfig.getKeyLabel(),
                HsmCypherMode.CBC);
    }

    /**
     * Creates an HsmCryptHelper bean.
     * <p>
     * This bean is only created when HsmCrypt is available.
     * 
     * @param hsmCrypt
     *            the HsmCrypt instance
     * @return an HsmCryptHelper instance
     */
    @Bean
    @ConditionalOnMissingBean(HsmCryptHelper.class)
    @ConditionalOnProperty(prefix = "hsmcrypt.encryption", name = "enabled", havingValue = "true")
    public HsmCryptHelper hsmCryptHelper(HsmCrypt hsmCrypt) {
        return new HsmCryptHelper(hsmCrypt);
    }

    /**
     * Creates the property resolver for auto-decryption.
     * <p>
     * This bean is only created when encryption is enabled.
     * 
     * @return an EncryptablePropertyResolver instance
     */
    @Bean
    @ConditionalOnProperty(prefix = "hsmcrypt.encryption", name = "enabled", havingValue = "true")
    public EncryptablePropertyResolver encryptablePropertyResolver() {
        return new EncryptablePropertyResolver();
    }

    /**
     * Ensures that the AES encryption key exists in HSM.
     * 
     * @param sessionFactory
     *            the HSM session factory
     * @param tokenLabel
     *            the token label
     * @param keyLabel
     *            the key label to check
     */
    private void ensureKeyExists(HsmSessionFactory sessionFactory, String tokenLabel, String keyLabel) {
        try {
            // Try to find the AES key
            try (HsmSession session = sessionFactory.getHsmSession()) {
                session.findHsmKey(keyLabel, HsmKeyType.AES);
            }
        } catch (TokenException e) {
            // Key not found - throw clear error message
            throw new RuntimeException(
                    String.format("HSM AES key not found: %s in token: %s. Please create the key manually.",
                            keyLabel, tokenLabel),
                    e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check HSM key existence: " + keyLabel, e);
        }
    }
}
