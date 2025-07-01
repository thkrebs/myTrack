package com.tmv.core.util;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncryption {
    public static void main(String[] args) {
        // Erstelle eine Instanz des Passwort-Encoders
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Roher Passworttext, der verschlüsselt werden soll
        String rawPassword = "justfordemo"; // Ersetze durch dein echtes Passwort

        // Das verschlüsselte Passwort
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Zeige das verschlüsselte Passwort in der Konsole an
        System.out.println("Verschlüsseltes Passwort: " + encodedPassword);
    }
}