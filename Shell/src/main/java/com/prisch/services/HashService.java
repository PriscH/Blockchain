package com.prisch.services;

import com.prisch.global.Constants;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashService {

    public String hash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] publicKeyHash = digest.digest(content.getBytes());
        return Base64.getEncoder().encodeToString(publicKeyHash).substring(0, Constants.HASH_LENGTH);
    }

}
