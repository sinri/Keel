package io.github.sinri.keel.test.lab.smtp;

import io.github.sinri.keel.email.smtp.KeelSmtpKit;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class TestSmtp {
    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        KeelSmtpKit keelSmtpKit = new KeelSmtpKit("test");
        System.out.println(keelSmtpKit.getMailConfig().toJson());
        Keel.getVertx().close();
    }
}
