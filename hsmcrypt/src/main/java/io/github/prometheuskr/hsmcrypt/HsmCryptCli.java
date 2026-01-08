package io.github.prometheuskr.hsmcrypt;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Command-line interface for HsmCrypt encryption/verification operations.
 * <p>
 * This CLI provides two main commands:
 * <ul>
 * <li><b>enc</b> - Encrypts plain text and outputs HCENC(...) formatted
 * result</li>
 * <li><b>vrf</b> - Verifies if encrypted value matches plain text (format:
 * "plain:HCENC(...)")</li>
 * </ul>
 * <p>
 * Usage examples:
 * 
 * <pre>
 * java -jar hsmcrypt-1.8.0-exec.jar enc "myPassword"
 * java -jar hsmcrypt-1.8.0-exec.jar vrf "myPassword:HCENC(ABC123...)"
 * </pre>
 * <p>
 * Requires application.yml with HSM configuration in the current directory.
 * If not found, a template will be generated automatically.
 * 
 * @author Prometheus
 * @see HsmCryptHelper
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.prometheuskr")
public class HsmCryptCli implements CommandLineRunner {

    private static final String VERSION = "1.21.0";

    private final HsmCryptHelper hsmCryptHelper;

    /**
     * Creates a new CLI instance with the given helper.
     * 
     * @param hsmCryptHelper the helper for encryption operations
     */
    public HsmCryptCli(HsmCryptHelper hsmCryptHelper) {
        this.hsmCryptHelper = hsmCryptHelper;
    }

    /**
     * Main entry point for the CLI application.
     * <p>
     * Checks for application.yml existence and creates a template if missing.
     * 
     * @param args command line arguments (enc/vrf and value)
     */
    public static void main(String[] args) {
        // Create application.yml if it doesn't exist and exit
        if (ensureConfigFileExists()) {
            System.out.println("\nApplication configuration file created.");
            System.out.println("Please edit 'application.yml' to configure your HSM settings,");
            System.out.println("then run the command again.\n");
            System.exit(0);
        }

        SpringApplication.run(HsmCryptCli.class, args);
    }

    /**
     * Ensures application.yml exists in current directory.
     * <p>
     * If the file doesn't exist, creates it with a template configuration.
     * 
     * @return true if file was created, false if already exists
     */
    private static boolean ensureConfigFileExists() {
        try {
            var configPath = Paths.get("application.yml");

            if (!Files.exists(configPath)) {
                var template = generateApplicationYmlTemplate();
                Files.write(configPath, template.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error: Could not create application.yml: " + e.getMessage());
            System.exit(1);
            return false;
        }
    }

    /**
     * Generates application.yml template content with default HSM configuration.
     * 
     * @return YAML template content as string
     */
    private static String generateApplicationYmlTemplate() {
        return """
                # Sipwon HSM Configuration (provided by sipwon-spring-boot-starter)
                sipwon:
                  # PKCS#11 library path
                  pkcs11-library-path: /Program Files/Safenet/ProtectToolkit 7/C SDK/bin/sw/cryptoki.dll

                  # HSM token configuration
                  token-label-and-pin:
                    - token-label: HSMCRYPT
                      pin: 1111

                # HsmCrypt Encryption Configuration
                hsmcrypt:
                  # Enable encryption functionality
                  encryption:
                    enabled: true
                    token-label: "HSMCRYPT"
                    key-label: "HsmCryptKey"

                # Spring Boot Configuration
                spring:
                  main:
                    banner-mode: off
                """;
    }

    /**
     * Executes the CLI command based on arguments.
     * <p>
     * Supported commands:
     * <ul>
     * <li>enc - Encrypts text and outputs HCENC(...) format</li>
     * <li>vrf - Verifies plaintext:encrypted pair</li>
     * <li>help - Displays usage information</li>
     * <li>version - Displays version information</li>
     * </ul>
     * 
     * @param args command line arguments
     * @throws Exception if command execution fails
     */
    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "enc" -> handleEncrypt(args);
            case "vrf" -> handleVerify(args);
            case "help", "-h", "--help" -> printUsage();
            case "version", "-v", "--version" -> System.out.println("HsmCrypt CLI version " + VERSION);
            default -> {
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
            }
        }
    }

    /**
     * Handles the encrypt command.
     * <p>
     * Encrypts the input text and outputs it in HCENC(...) format.
     * 
     * @param args command line arguments containing the text to encrypt
     * @throws Exception if encryption fails
     */
    private void handleEncrypt(String[] args) throws Exception {
        String input = parseArgs(args);

        if (input == null || input.isEmpty()) {
            System.err.println("Error: Input text is required");
            printUsage();
            System.exit(1);
        }

        String encrypted = hsmCryptHelper.encryptWithFormat(input);
        System.out.println(encrypted);
    }

    /**
     * Handles the verify command.
     * <p>
     * Verifies if the encrypted value matches the plaintext.
     * Input format: "plaintext:HCENC(...)"
     * 
     * @param args command line arguments containing plaintext:encrypted pair
     * @throws Exception if verification fails
     */
    private void handleVerify(String[] args) throws Exception {
        String input = parseArgs(args);

        if (input == null || input.isEmpty()) {
            System.err.println("Error: Input text is required");
            printUsage();
            System.exit(1);
        }

        try {
            // Find the last HCENC( position (encrypted text is always at the end)
            int hcencIndex = input.lastIndexOf(HsmCryptProperties.DEFAULT_PREFIX);
            if (hcencIndex == -1) {
                System.out.println("Invalid: HCENC() format not found");
                System.exit(1);
            }

            // Find the last colon before HCENC(
            int colonIndex = input.lastIndexOf(":", hcencIndex);
            if (colonIndex == -1) {
                System.out.println("Invalid: Input must be in 'plaintext:HCENC(...) format");
                System.exit(1);
            }

            // Split plaintext and encrypted parts
            String plaintext = input.substring(0, colonIndex);
            String encrypted = input.substring(colonIndex + 1);

            // Verify HCENC() format
            if (!encrypted.endsWith(HsmCryptProperties.DEFAULT_SUFFIX)) {
                System.out.println("Invalid: Encrypted part is not in HCENC() format");
                System.exit(1);
            }

            // Decrypt and compare
            String decrypted = hsmCryptHelper.decryptIfEncrypted(encrypted);

            if (plaintext.equals(decrypted)) {
                System.out.println("Valid");
            } else {
                System.out.println("Invalid");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Invalid: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parses command line arguments.
     * 
     * @param args the command line arguments to parse
     * @return the input text from arguments, or null if not provided
     * @throws IllegalArgumentException if too many arguments provided
     */
    private String parseArgs(String[] args) throws Exception {
        // Exactly one argument is required after the command
        if (args.length == 2) {
            return args[1];
        } else if (args.length > 2) {
            throw new IllegalArgumentException("Too many arguments. Use quotes for text with spaces.");
        }

        return null;
    }

    /**
     * Prints usage information and examples to console.
     */
    private void printUsage() {
        System.out.println("HsmCrypt CLI - HSM-based encryption/verification tool");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar hsmcrypt.jar <command> <text>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  enc <text>                Encrypt text (outputs in " + HsmCryptProperties.DEFAULT_PREFIX
                + "..." + HsmCryptProperties.DEFAULT_SUFFIX + " format)");
        System.out.println("  vrf <plaintext:encrypted> Verify plaintext:encrypted pair");
        System.out.println("  help                      Show this help message");
        System.out.println("  version                   Show version information");
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  HSM settings are read from application.yml in the current directory.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Encrypt text");
        System.out.println("  java -jar hsmcrypt.jar enc \"Hello World\"");
        System.out.println();
        System.out.println("  # Verify plaintext and encrypted value match");
        System.out.println("  java -jar hsmcrypt.jar vrf \"Hello World:" + HsmCryptProperties.DEFAULT_PREFIX + "..."
                + HsmCryptProperties.DEFAULT_SUFFIX + "\"");
    }
}
