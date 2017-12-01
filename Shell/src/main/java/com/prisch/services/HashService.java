package com.prisch.services;

import com.prisch.global.Constants;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashService {

    public String hash(String content) throws NoSuchAlgorithmException {
        String fullHash = hashWithoutTrunc(content);
        return trunc(fullHash);
    }

    public String hashWithoutTrunc(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashStream = digest.digest(content.getBytes());
        return Base64.getEncoder().encodeToString(hashStream);
    }

    public String trunc(String fullHash) {
        return fullHash.substring(fullHash.length() - Constants.HASH_LENGTH);
    }

}
