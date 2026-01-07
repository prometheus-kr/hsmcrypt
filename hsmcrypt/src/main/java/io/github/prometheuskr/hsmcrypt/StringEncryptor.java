package io.github.prometheuskr.hsmcrypt;

/**
 * Interface for String decryption operations.
 * <p>
 * This interface provides decryption functionality for library usage.
 * For encryption, use the CLI tool: java -jar hsmcrypt.jar enc "text"
 * <p>
 * Package-private: Only accessible within hsmcrypt package.
 * External applications should use property resolver feature (HCENC values in
 * properties).
 * 
 * @author Prometheus
 */
interface StringEncryptor {

    /**
     * Decrypts an encrypted message.
     * 
     * @param encryptedMessage
     *                         the encrypted text to decrypt
     * @return the decrypted plain text
     */
    String decrypt(String encryptedMessage);
}
