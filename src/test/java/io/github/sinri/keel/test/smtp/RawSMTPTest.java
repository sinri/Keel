package io.github.sinri.keel.test.smtp;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;

public class RawSMTPTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        MailConfig config = new MailConfig();
        config.setHostname(Keel.getPropertiesReader().getProperty("email.smtp.test.hostname"));
        config.setPort(Integer.parseInt(Keel.getPropertiesReader().getProperty("email.smtp.test.port")));
        if ("ON".equalsIgnoreCase(Keel.getPropertiesReader().getProperty("email.smtp.test.starttls"))) {
            config.setStarttls(StartTLSOptions.REQUIRED);
        }
        if ("ON".equalsIgnoreCase(Keel.getPropertiesReader().getProperty("email.smtp.test.ssl"))) {
            config.setSsl(true);
        }
        config.setUsername(Keel.getPropertiesReader().getProperty("email.smtp.test.username"));
        config.setPassword(Keel.getPropertiesReader().getProperty("email.smtp.test.password"));

        MailClient mailClient = MailClient.create(Keel.getVertx(), config);

        MailMessage message = new MailMessage();
        message.setFrom("oc@leqee.com (Keel Project)");
        message.setTo("ljni@leqee.com");
        message.setCc("Another User <ever.stray@hotmail.com>");
        message.setSubject("TEST MAIL");
        message.setText("this is the plain message text");
        message.setHtml("this is html text <a href=\"https://vertx.io\">vertx.io</a>");


        mailClient.sendMail(message)
                .compose(mailResult -> {
                    Keel.outputLogger("smtp").notice("MSG ID: " + mailResult.getMessageID());
                    Keel.outputLogger("smtp").notice("getRecipients as following:");
                    for (var x : mailResult.getRecipients()) {
                        Keel.outputLogger("smtp").notice("Recipient: " + x);
                    }
                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    Keel.outputLogger("smtp").exception(throwable);
                })
                .eventually(v -> {
                    Keel.getVertx().close();
                    return Future.succeededFuture();
                });
    }
}
