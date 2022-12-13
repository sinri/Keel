package io.github.sinri.keel.test.lab.authenticator;

import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticator;

public class GATest2 {
    public static void main(String[] args) {
        var shared_secret = "RYVRB75HBI2D3RKN76QWPA3IY4LWJBRJ";
        var ga = new GoogleAuthenticator();
        // generate TOTP
        while (true) {
            var totp = ga.getTotpPassword(shared_secret);
            System.out.println(System.currentTimeMillis() + " TOTP " + totp);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
