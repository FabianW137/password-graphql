package com.example.pwm.graphql.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class CryptoService {

    private final byte[] masterKeyBytes;
    private final SecureRandom rng = new SecureRandom();

    public CryptoService(String masterKeyB64) {
        if (masterKeyB64 == null || masterKeyB64.isBlank()) {
            throw new IllegalStateException("Missing property crypto.master (Base64 AES key)");
        }
        this.masterKeyBytes = Base64.getDecoder().decode(masterKeyB64);
        if (this.masterKeyBytes.length < 32) {
            throw new IllegalStateException("crypto.master must be 32 bytes (base64 of 256-bit key)");
        }
    }

    // stabile, owner-gebundene Ableitung aus Master-Key (PBKDF2; 256 Bit AES-Key)
    private SecretKey deriveKey(UUID ownerId) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    Base64.getEncoder().encodeToString(masterKeyBytes).toCharArray(),
                    ownerId.toString().getBytes(StandardCharsets.UTF_8),
                    120_000, 256
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("deriveKey failed", e);
        }
    }

    public boolean isEncrypted(String s) {
        return s != null && s.startsWith("enc:v1:");
    }

    public String ensureEncrypted(UUID ownerId, String maybePlaintext) {
        if (maybePlaintext == null || maybePlaintext.isEmpty()) return "";
        return isEncrypted(maybePlaintext) ? maybePlaintext : encrypt(ownerId, maybePlaintext);
    }

    public String encrypt(UUID ownerId, String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        try {
            byte[] iv = new byte[12];
            rng.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey key = deriveKey(ownerId);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return "enc:v1:" +
                    Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(ct);
        } catch (Exception e) {
            throw new RuntimeException("encrypt failed", e);
        }
    }

    public String decrypt(UUID ownerId, String wrapped) {
        if (wrapped == null || wrapped.isEmpty()) return "";
        if (!isEncrypted(wrapped)) return wrapped;
        try {
            String[] parts = wrapped.split(":", 4); // enc:v1:<ivB64>:<ctB64>
            byte[] iv = Base64.getDecoder().decode(parts[2]);
            byte[] ct = Base64.getDecoder().decode(parts[3]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey key = deriveKey(ownerId);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("decrypt failed", e);
        }
    }
}
