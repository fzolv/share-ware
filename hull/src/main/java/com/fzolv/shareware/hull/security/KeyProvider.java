package com.fzolv.shareware.hull.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyProvider {

    private static final String PRIVATE_KEY_PATH = "credentials/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "credentials/public_key.pem";

    private volatile RSAPrivateKey fallbackPrivateKey;
    private volatile RSAPublicKey fallbackPublicKey;

    public RSAPrivateKey getPrivateKey() {
        try {
            String pem = readClasspathPem(PRIVATE_KEY_PATH);
            byte[] der = parsePem(pem, "PRIVATE KEY");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(der));
            return (RSAPrivateKey) key;
        } catch (IOException | GeneralSecurityException e) {
            ensureFallbackKeysInitialized();
            System.err.println("[KeyProvider] Warning: Using in-memory generated RSA private key (dev fallback). Place keys at src/main/resources/" + PRIVATE_KEY_PATH);
            return fallbackPrivateKey;
        }
    }

    public RSAPublicKey getPublicKey() {
        try {
            String pem = readClasspathPem(PUBLIC_KEY_PATH);
            byte[] der = parsePem(pem, "PUBLIC KEY");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey key = kf.generatePublic(new X509EncodedKeySpec(der));
            return (RSAPublicKey) key;
        } catch (IOException | GeneralSecurityException e) {
            ensureFallbackKeysInitialized();
            System.err.println("[KeyProvider] Warning: Using in-memory generated RSA public key (dev fallback). Place keys at src/main/resources/" + PUBLIC_KEY_PATH);
            return fallbackPublicKey;
        }
    }

    private static String readClasspathPem(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    private static byte[] parsePem(String pem, String type) {
        String header = "-----BEGIN " + type + "-----";
        String footer = "-----END " + type + "-----";
        String base64 = pem.replace(header, "")
                .replace(footer, "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private synchronized void ensureFallbackKeysInitialized() {
        if (fallbackPrivateKey != null && fallbackPublicKey != null) {
            return;
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            this.fallbackPrivateKey = (RSAPrivateKey) pair.getPrivate();
            this.fallbackPublicKey = (RSAPublicKey) pair.getPublic();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to generate fallback RSA keypair", ex);
        }
    }
}


