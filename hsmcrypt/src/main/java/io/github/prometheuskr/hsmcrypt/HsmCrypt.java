package io.github.prometheuskr.hsmcrypt;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;

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
class HsmCrypt {

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
    HsmCrypt(HsmSessionFactory sessionFactory, String tokenLabel, String keyLabel) {
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
    HsmCrypt(HsmSessionFactory sessionFactory, String tokenLabel, String keyLabel,
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
    String decrypt(String encryptedText) {
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
     * Encodes a string to hexadecimal with random prefix and padding.
     * Adds a random prefix block at the beginning for randomization, then applies
     * ISO/IEC 9797-1 Padding Method 2: append 0x80 followed by 0x00 bytes.
     * The random prefix is XORed with the actual data for additional obfuscation.
     * 
     * @param str
     *            the string to encode
     * @return hexadecimal string with random prefix and padding
     */
    private String encodeWithRandomizationAndPadding(String str) {
        var hexFormat = HexFormat.of();

        // Generate random prefix block
        var random = new SecureRandom();
        var randomBlock = new byte[RANDOM_PREFIX_BYTES];
        random.nextBytes(randomBlock);

        // Convert actual data to bytes
        var dataBytes = str.getBytes(StandardCharsets.UTF_8);

        // Calculate total size with padding (including random prefix)
        int totalDataSize = RANDOM_PREFIX_BYTES + dataBytes.length + 1; // random + data + 0x80
        int paddingSize = (16 - (totalDataSize % 16)) % 16;
        int totalSize = dataBytes.length + 1 + paddingSize;

        // Create buffer for data + padding
        var buffer = new byte[totalSize];
        System.arraycopy(dataBytes, 0, buffer, 0, dataBytes.length);
        buffer[dataBytes.length] = (byte) 0x80; // ISO/IEC 9797-1 padding
        // Remaining bytes are already 0x00

        // XOR buffer with random prefix (repeating)
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] ^= randomBlock[i % RANDOM_PREFIX_BYTES];
        }

        // Build final hex string: random prefix + XORed data
        var hex = new StringBuilder();
        hex.append(hexFormat.formatHex(randomBlock));
        hex.append(hexFormat.formatHex(buffer));

        return hex.toString();
    }

    /**
     * Decodes a hexadecimal string by removing random prefix and padding.
     * Removes the random prefix block and ISO/IEC 9797-1 Padding Method 2:
     * 0x80 and trailing 0x00 bytes.
     * The random prefix is XORed with the data to restore the original content.
     * 
     * @param hex
     *            the hexadecimal string to decode
     * @return decoded string
     */
    private String decodeWithRandomizationAndPadding(String hex) {
        var hexFormat = HexFormat.of();

        // Extract random prefix
        if (hex.length() <= RANDOM_PREFIX_HEX_LENGTH) {
            throw new HsmCryptException("Invalid encrypted data: too short");
        }

        String randomHex = hex.substring(0, RANDOM_PREFIX_HEX_LENGTH);
        String xoredDataHex = hex.substring(RANDOM_PREFIX_HEX_LENGTH);

        var randomBlock = hexFormat.parseHex(randomHex);
        var xoredData = hexFormat.parseHex(xoredDataHex);

        // XOR back to restore original data
        for (int i = 0; i < xoredData.length; i++) {
            xoredData[i] ^= randomBlock[i % RANDOM_PREFIX_BYTES];
        }

        // Remove padding: find 0x80 and remove it and all trailing 0x00s
        int paddingStart = -1;
        for (int i = xoredData.length - 1; i >= 0; i--) {
            if (xoredData[i] == (byte) 0x80) {
                // Verify everything after this is 0x00
                boolean validPadding = true;
                for (int j = i + 1; j < xoredData.length; j++) {
                    if (xoredData[j] != 0x00) {
                        validPadding = false;
                        break;
                    }
                }
                if (validPadding) {
                    paddingStart = i;
                    break;
                }
            }
        }

        if (paddingStart < 0) {
            throw new HsmCryptException("Invalid padding: 0x80 marker not found");
        }

        // Extract actual data (before padding)
        var actualData = new byte[paddingStart];
        System.arraycopy(xoredData, 0, actualData, 0, paddingStart);

        return new String(actualData, StandardCharsets.UTF_8);
    }
}
