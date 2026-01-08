package io.github.prometheuskr.hsmcrypt.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Example application using the HsmCrypt library
 * Demonstrates automatic decryption of values in HCENC() format.
 * 
 * @author prometheus-kr
 */
@SpringBootApplication
public class HsmCryptExampleApplication {

    @Value("${app.plain}")
    private String plainValue;

    @Value("${app.encrypted}")
    private String encryptedValue;

    public static void main(String[] args) {
        SpringApplication.run(HsmCryptExampleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(Environment env) {
        return args -> {
            String plain = env.getProperty("app.plain");
            String encrypted = env.getProperty("app.encrypted");

            System.out.println("\n=== HsmCrypt Property Resolver Example ===");
            System.out.println("Using Environment.getProperty():");
            System.out.println("  app.plain     = " + plain);
            System.out.println("  app.encrypted = " + encrypted);
            System.out.println("  Match         = " + (plain.equals(encrypted) ? "YES" : "NO"));

            System.out.println("\nUsing @Value injection:");
            System.out.println("  app.plain     = " + plainValue);
            System.out.println("  app.encrypted = " + encryptedValue);
            System.out.println("  Match         = " + (plainValue.equals(encryptedValue) ? "YES" : "NO"));

            System.out.println("\nNote: app.encrypted is stored as HCENC(...) in application.yml");
            System.out.println("      and automatically decrypted by EncryptablePropertyResolver");
        };
    }
}
