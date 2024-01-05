package io.github.sinri.keel.test.lab.smtp;

import io.github.sinri.keel.email.smtp.KeelSmtpKit;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.keel;

public class TestSmtp {
    public static void main(String[] args) {
        keel.initializeVertxStandalone(new VertxOptions());
        keel.getConfiguration().loadPropertiesFile("config.properties");
        KeelSmtpKit keelSmtpKit = new KeelSmtpKit("test");
        System.out.println(keelSmtpKit.getMailConfig().toJson());
        keel.getVertx().close();
    }
}
