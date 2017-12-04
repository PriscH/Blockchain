package com.prisch.services;

import com.prisch.global.Constants;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyService {

    public boolean checkKeysExist() {
        return Files.exists(Constants.PUBLIC_KEY_PATH) && Files.exists(Constants.PRIVATE_KEY_PATH);
    }

    public String readPublicKey() throws IOException {
        return new String(Files.readAllBytes(Constants.PUBLIC_KEY_PATH));
    }

    public String readPrivateKey() throws IOException {
        return new String(Files.readAllBytes(Constants.PRIVATE_KEY_PATH));
    }

    public String getAddress() throws NoSuchAlgorithmException, IOException {
        String publicKey = readPublicKey();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] publicKeyHashStream = digest.digest(publicKey.getBytes());
        String publicKeyHash = Base64.getEncoder().encodeToString(publicKeyHashStream);

        return publicKeyHash.substring(publicKeyHash.length() - Constants.ADDRESS_LENGTH);
    }

    public String sign(String content) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        String privateKeyContent = new String(Files.readAllBytes(Constants.PRIVATE_KEY_PATH));

        KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes());

        byte[] signatureStream = signature.sign();
        return Base64.getEncoder().encodeToString(signatureStream);
    }

}
