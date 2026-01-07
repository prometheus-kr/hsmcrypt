package io.github.prometheuskr.hsmcrypt.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Example application using the HsmCrypt library
 * Demonstrates automatic decryption of values in HCENC() format.
 * 
 * @author prometheus-kr
 */
@SpringBootApplication
public class HsmCryptExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsmCryptExampleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(@Value("${app.plain}") String plain,
            @Value("${app.encrypted}") String encrypted) {
        return args -> {
            System.out.println("\n=== HsmCrypt Property Resolver Example ===");
            System.out.println("app.plain     = " + plain);
            System.out.println("app.encrypted = " + encrypted);
            System.out.println("Match         = " + (plain.equals(encrypted) ? "YES" : "NO"));
            System.out.println("\nNote: app.encrypted is stored as HCENC(...) in application.yml");
            System.out.println("      and automatically decrypted by EncryptablePropertyResolver");
        };
    }
}
