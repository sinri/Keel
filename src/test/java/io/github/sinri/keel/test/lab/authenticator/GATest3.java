package io.github.sinri.keel.test.lab.authenticator;

import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticator;
import io.github.sinri.keel.core.helper.authenticator.googleauth.GoogleAuthenticatorConfig;

public class GATest3 {
    public static void main(String[] args) {
        var shared_secret = "RYVRB75HBI2D3RKN76QWPA3IY4LWJBRJ";
        var config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setWindowSize(1)
                .build();
        var ga = new GoogleAuthenticator(config);
        // verify TOTP
        int totp = 748646;
        while (true) {
            var ok = ga.authorize(shared_secret, totp);
            System.out.println("TOTP " + totp + " FOR " + System.currentTimeMillis() + " : " + ok);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // 1669979829756-1669979764544
    }
}
