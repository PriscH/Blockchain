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

    private String address;

    public boolean checkKeysExist() {
        return Files.exists(Constants.PUBLIC_KEY_PATH) && Files.exists(Constants.PRIVATE_KEY_PATH);
    }

    public String readPublicKey(){
        try {
            return new String(Files.readAllBytes(Constants.PUBLIC_KEY_PATH));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String readPrivateKey() {
        try {
            return new String(Files.readAllBytes(Constants.PRIVATE_KEY_PATH));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getAddress() {
        if (address == null) {
            String publicKey = readPublicKey();

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] publicKeyHashStream = digest.digest(publicKey.getBytes());
                String publicKeyHash = Base64.getEncoder().encodeToString(publicKeyHashStream);

                address = publicKeyHash.substring(publicKeyHash.length() - Constants.ADDRESS_LENGTH);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }

        return address;
    }

    public void resetAddress() {
        address = null;
    }

    public String sign(String content) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        String privateKeyContent = readPrivateKey();

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
