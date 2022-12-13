package io.github.sinri.keel.test.lab.authenticator;

import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticator;
import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticatorKey;

public class GATest1 {
    static GoogleAuthenticator gAuth;
    static GoogleAuthenticatorKey key;

    public static void main(String[] args) {
        gAuth = new GoogleAuthenticator();
        key = gAuth.createCredentials();

        generateKey();

        var code = generateTOTP();

        validateCode(code);
    }

    private static void generateKey() {

        System.out.println("The user should be given the value of the shared secret: " + key.getKey());
    }

    private static boolean validateCode(int verificationCode) {
        // The following code
        // checks the validity of the specified verificationCode against the provided Base32-encoded secretKey:
        boolean isCodeValid = gAuth.authorize(key.getKey(), verificationCode);
        System.out.println("verificationCode validated: " + isCodeValid);
        return isCodeValid;
    }

    private static int generateTOTP() {
        var secretKey = key.getKey();
        int code = gAuth.getTotpPassword(secretKey);
        System.out.println("TOTP: " + code);
        return code;
    }
}
