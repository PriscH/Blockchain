package com.prisch.assignments.assignment1;

import org.springframework.stereotype.Component;

@Component
public class SignatureVerifier {

    public boolean verifySignature(String publicKeyString, String signatureValue, String message) {
        // TODO: [1B]
        // Implement this method, it should verify that the signatureValue is a valid signature for the provided message based on the publicKeyString
        // Hint: Have a look at {@link KeyService.sign}, this method should be very similar
        // Hint: While the private key is PKCS8 encoded, the same isn't true for the public key

        return false;
    }
}
