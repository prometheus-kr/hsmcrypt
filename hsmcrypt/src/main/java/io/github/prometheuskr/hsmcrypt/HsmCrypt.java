package io.github.prometheuskr.hsmcrypt;

import iaik.pkcs.pkcs11.TokenException;
import io.github.prometheuskr.sipwon.constant.HsmKeyType;
import io.github.prometheuskr.sipwon.constant.HsmMechanism;
import io.github.prometheuskr.sipwon.key.HsmKey;
import io.github.prometheuskr.sipwon.session.HsmSession;
import io.github.prometheuskr.sipwon.session.HsmSessionFactory;

/**
 * HSM-based encryptor similar to Jasypt functionality.
 * <p>
 * Provides AES encryption and decryption capabilities using HSM (Hardware
 * Security
 * Module) through the Sipwon library. This class acts as a high-level API for
 * secure
 * cryptographic operations, similar to how Jasypt provides encryption services.
 * <p>
 * Only supports AES encryption with CBC mechanism.
 * <p>
 * Package-private: Only accessible within hsmcrypt package.
 * External applications should use property resolver feature (HCENC values in
 * properties).
 * 
 * @author Prometheus
 */
class HsmCrypt implements StringEncryptor {

    /** Random prefix size in bytes for non-deterministic encryption */
    private static final int RANDOM_PREFIX_BYTES = 8;
    /** Random prefix size in hexadecimal characters (2 chars per byte) */
    private static final int RANDOM_PREFIX_HEX_LENGTH = RANDOM_PREFIX_BYTES * 2;

    private final HsmSessionFactory sessionFactory;
    private final String keyLabel;
    private final String tokenLabel;
    private final HsmMechanism mechanism;

    /**
     * Creates a HsmCrypt with default AES CBC mechanism.
     * 
     * @param sessionFactory
     *                       the HSM session factory
     * @param tokenLabel
     *                       the token label to use
     * @param keyLabel
     *                       the key label to use for encryption/decryption
     */
    public HsmCrypt(HsmSessionFactory sessionFactory, String tokenLabel, String keyLabel) {
        this(sessionFactory, tokenLabel, keyLabel, HsmMechanism.AES_CBC);
    }

    /**
     * Creates a HsmCrypt with custom AES mechanism.
     * 
     * @param sessionFactory
     *                       the HSM session factory
     * @param tokenLabel
     *                       the token label to use
     * @param keyLabel
     *                       the key label to use for encryption/decryption
     * @param mechanism
     *                       the AES encryption mechanism to use
     */
    public HsmCrypt(HsmSessionFactory sessionFactory, String tokenLabel, String keyLabel,
            HsmMechanism mechanism) {
        if (sessionFactory == null) {
            throw new IllegalArgumentException("sessionFactory cannot be null");
        }
        if (tokenLabel == null || tokenLabel.isEmpty()) {
            throw new IllegalArgumentException("tokenLabel cannot be null or empty");
        }
        if (keyLabel == null || keyLabel.isEmpty()) {
            throw new IllegalArgumentException("keyLabel cannot be null or empty");
        }
        if (mechanism == null) {
            throw new IllegalArgumentException("mechanism cannot be null");
        }

        this.sessionFactory = sessionFactory;
        this.tokenLabel = tokenLabel;
        this.keyLabel = keyLabel;
        this.mechanism = mechanism;
    }

    /**
     * Encrypts the given plaintext string.
     * Package-private: For internal use by HsmCryptHelper only.
     * Use CLI for encryption: java -jar hsmcrypt.jar enc "text"
     * 
     * @param plainText
     *                  the text to encrypt
     * @return the encrypted text as a hexadecimal string
     * @throws HsmCryptException
     *                           if encryption fails
     */
    String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try (HsmSession session = sessionFactory.getHsmSession(tokenLabel)) {
            HsmKey key = session.findHsmKey(keyLabel, HsmKeyType.AES);
            // Convert plaintext to hex string with padding
            String hexPlainText = encodeWithRandomizationAndPadding(plainText);
            return key.encrypt(hexPlainText, mechanism);
        } catch (TokenException e) {
            throw new HsmCryptException("Failed to encrypt data", e);
        } catch (Exception e) {
            throw new HsmCryptException("Unexpected error during encryption", e);
        }
    }

    /**
     * Decrypts the given encrypted string.
     * 
     * @param encryptedText
     *                      the encrypted text as a hexadecimal string
     * @return the decrypted plaintext
     * @throws HsmCryptException
     *                           if decryption fails
     */
    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }

        try (HsmSession session = sessionFactory.getHsmSession(tokenLabel)) {
            HsmKey key = session.findHsmKey(keyLabel, HsmKeyType.AES);
            String hexDecrypted = key.decrypt(encryptedText, mechanism);
            // Convert hex string back to plaintext
            return decodeWithRandomizationAndPadding(hexDecrypted);
        } catch (TokenException e) {
            throw new HsmCryptException("Failed to decrypt data", e);
        } catch (Exception e) {
            throw new HsmCryptException("Unexpected error during decryption", e);
        }
    }

    /**
     * Gets the token label being used.
     * 
     * @return the token label
     */
    public String getTokenLabel() {
        return tokenLabel;
    }

    /**
     * Gets the key label being used.
     * 
     * @return the key label
     */
    public String getKeyLabel() {
        return keyLabel;
    }

    /**
     * Gets the encryption mechanism being used.
     * 
     * @return the mechanism
     */
    public HsmMechanism getMechanism() {
        return mechanism;
    }

    /**
     * Encodes a string to hexadecimal with random prefix and padding.
     * Adds a random prefix block at the beginning for randomization, then applies
     * ISO/IEC 9797-1 Padding Method 2: append 0x80 followed by 0x00 bytes.
     * 
     * @param str the string to encode
     * @return hexadecimal string with random prefix and padding
     */
    private String encodeWithRandomizationAndPadding(String str) {
        StringBuilder hex = new StringBuilder();

        // Add random first block
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] randomBlock = new byte[RANDOM_PREFIX_BYTES];
        random.nextBytes(randomBlock);
        for (byte b : randomBlock) {
            hex.append(String.format("%02x", b));
        }

        // Add actual data
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        // Add padding: 80 followed by 00s to make it multiple of 32 hex chars (16 bytes
        // for AES)
        hex.append("80");
        while (hex.length() % 32 != 0) {
            hex.append("00");
        }

        return hex.toString();
    }

    /**
     * Decodes a hexadecimal string by removing random prefix and padding.
     * Removes the random prefix block and ISO/IEC 9797-1 Padding Method 2:
     * 0x80 and trailing 0x00 bytes.
     * 
     * @param hex the hexadecimal string to decode
     * @return decoded string
     */
    private String decodeWithRandomizationAndPadding(String hex) {
        // Remove first random block
        if (hex.length() > RANDOM_PREFIX_HEX_LENGTH) {
            hex = hex.substring(RANDOM_PREFIX_HEX_LENGTH);
        }

        // Remove padding: find last 80 and remove it and all trailing 00s
        int paddingStart = hex.lastIndexOf("80");
        if (paddingStart > 0) {
            // Check if everything after 80 is 00
            boolean validPadding = true;
            for (int i = paddingStart + 2; i < hex.length(); i += 2) {
                if (!hex.substring(i, i + 2).equals("00")) {
                    validPadding = false;
                    break;
                }
            }
            if (validPadding) {
                hex = hex.substring(0, paddingStart);
            }
        }

        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}
