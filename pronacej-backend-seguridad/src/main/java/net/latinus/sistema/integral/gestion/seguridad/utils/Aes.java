package net.latinus.sistema.integral.gestion.seguridad.utils;

import lombok.Data;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;


@Data
public class Aes {

    private final String ALGORITHM = "AES";

    public String encrypt(String password, String plainText) throws Exception {
        byte[] iv = new byte[12];
        byte[] salt = new byte[16];

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        // Concatenate salt + iv + ciphertext for storage/transmission
        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);

        // Output Base64
        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String password, String encryptedData) throws Exception {
        byte[] data = Base64.getDecoder().decode(encryptedData);

        byte[] salt = new byte[16];
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[data.length - 28];

        System.arraycopy(data, 0, salt, 0, 16);
        System.arraycopy(data, 16, iv, 0, 12);
        System.arraycopy(data, 28, ciphertext, 0, ciphertext.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] plainText = cipher.doFinal(ciphertext);
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
