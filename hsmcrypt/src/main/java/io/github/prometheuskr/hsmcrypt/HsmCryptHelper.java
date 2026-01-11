package io.github.prometheuskr.hsmcrypt;

/**
 * Helper class for encryption operations with HCENC(...) format support.
 * <p>
 * Provides utility methods for encrypting and decrypting strings
 * with a consistent format similar to Jasypt's ENC(...) notation.
 * <p>
 * Package-private: Only accessible within hsmcrypt package.
 * External applications should use property resolver feature (HCENC values in
 * properties).
 * 
 * @author Prometheus
 */
class HsmCryptHelper {
    private final HsmCrypt hsmCrypt;
    private final String prefix;
    private final String suffix;

    /**
     * Creates a new HsmCryptHelper with default HCENC(...) format.
     * 
     * @param hsmCrypt
     *            the HsmCrypt instance to use
     */
    public HsmCryptHelper(HsmCrypt hsmCrypt) {
        if (hsmCrypt == null) {
            throw new IllegalArgumentException("hsmCrypt cannot be null");
        }

        this.hsmCrypt = hsmCrypt;
        this.prefix = HsmCryptProperties.DEFAULT_PREFIX;
        this.suffix = HsmCryptProperties.DEFAULT_SUFFIX;
    }

    /**
     * Encrypts a plain text and wraps it with configured format (default:
     * HCENC(...)).
     * 
     * @param plainText
     *            the text to encrypt
     * @return the encrypted text in configured format
     */
    public String encryptWithFormat(String plainText) {
        if (plainText == null) {
            return null;
        }
        String encrypted = hsmCrypt.encrypt(plainText);
        return prefix + encrypted + suffix;
    }

    /**
     * Decrypts a text that may or may not be in the configured format.
     * If the text is not encrypted, it returns as-is.
     * 
     * @param text
     *            the text to decrypt
     * @return the decrypted text or original text if not encrypted
     */
    public String decryptIfEncrypted(String text) {
        return isEncrypted(text) //
                ? hsmCrypt.decrypt(text.substring(prefix.length(), text.length() - suffix.length())) //
                : text;
    }

    /**
     * Checks if the given text is in encrypted format.
     * 
     * @param text
     *            the text to check
     * @return true if the text is in encrypted format, false otherwise
     */
    private boolean isEncrypted(String text) {
        return text != null && text.startsWith(prefix) && text.endsWith(suffix);
    }
}
