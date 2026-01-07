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
     *                 the HsmCrypt instance to use
     */
    public HsmCryptHelper(HsmCrypt hsmCrypt) {
        this(hsmCrypt, "HCENC(", ")");
    }

    /**
     * Creates a new HsmCryptHelper with custom format.
     * 
     * @param hsmCrypt
     *                 the HsmCrypt instance to use
     * @param prefix
     *                 the prefix for encrypted values
     * @param suffix
     *                 the suffix for encrypted values
     */
    public HsmCryptHelper(HsmCrypt hsmCrypt, String prefix, String suffix) {
        if (hsmCrypt == null) {
            throw new IllegalArgumentException("hsmCrypt cannot be null");
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }
        if (suffix == null) {
            throw new IllegalArgumentException("suffix cannot be null");
        }

        this.hsmCrypt = hsmCrypt;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Encrypts a plain text and wraps it with configured format (default:
     * HCENC(...)).
     * 
     * @param plainText
     *                  the text to encrypt
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
     *             the text to decrypt
     * @return the decrypted text or original text if not encrypted
     */
    public String decryptIfEncrypted(String text) {
        if (text == null) {
            return null;
        }

        if (text.startsWith(prefix) && text.endsWith(suffix)) {
            String encryptedValue = text.substring(prefix.length(), text.length() - suffix.length());
            return hsmCrypt.decrypt(encryptedValue);
        }

        return text;
    }

    /**
     * Checks if the given text is in encrypted format.
     * 
     * @param text
     *             the text to check
     * @return true if the text is in encrypted format, false otherwise
     */
    public boolean isEncrypted(String text) {
        return text != null && text.startsWith(prefix) && text.endsWith(suffix);
    }

    /**
     * Gets the prefix used for encrypted values.
     * 
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix used for encrypted values.
     * 
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }
}
