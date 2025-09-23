package com.dawnsynch.darajaapi.utils;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;


@Component
public class Helper {
    public static String toBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String generateSecurityCredential(String initiatorPassword, String certPath) throws Exception {
        // Load certificate from resources
        InputStream inputStream = new ClassPathResource(certPath).getInputStream();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);
        PublicKey publicKey = certificate.getPublicKey();

        // Encrypt password with RSA/ECB/PKCS1Padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // PKCS#1 v1.5
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedPassword = cipher.doFinal(initiatorPassword.getBytes("UTF-8"));

        // Encode to Base64
        return Base64.getEncoder().encodeToString(encryptedPassword);
    }
}