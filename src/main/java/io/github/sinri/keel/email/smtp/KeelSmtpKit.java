package io.github.sinri.keel.email.smtp;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.KeelConfiguration;
import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;

import java.util.List;

/**
 * @since 1.10
 */
public class KeelSmtpKit {
    private final Keel keel;
    private final String smtpName;
    private final MailConfig mailConfig;
    private final MailClient mailClient;

    public KeelSmtpKit(Keel keel, String smtpName, boolean shared) {
        this.keel = keel;
        this.smtpName = smtpName;
        this.mailConfig = buildMailConfig(keel.getConfiguration(), smtpName);
        if (shared) {
            this.mailClient = MailClient.createShared(keel.getVertx(), this.mailConfig, smtpName);
        } else {
            this.mailClient = MailClient.create(keel.getVertx(), this.mailConfig);
        }
    }

    public KeelSmtpKit(Keel keel, String smtpName) {
        this.keel = keel;
        this.smtpName = smtpName;
        this.mailConfig = buildMailConfig(keel.getConfiguration(), smtpName);
        this.mailClient = MailClient.create(keel.getVertx(), this.mailConfig);
    }

    public KeelSmtpKit(Keel keel) {
        this.keel = keel;
        this.smtpName = keel.getConfiguration().readString("email", "smtp", "default_smtp_name");
        this.mailConfig = buildMailConfig(keel.getConfiguration(), smtpName);
        this.mailClient = MailClient.create(keel.getVertx(), this.mailConfig);
    }

    private static MailConfig buildMailConfig(KeelConfiguration keelConfiguration, String smtpName) {
        KeelConfiguration smtpConfiguration = keelConfiguration.extract("email", "smtp", smtpName);
        if (smtpConfiguration != null) {
            return new MailConfig(smtpConfiguration.toJsonObject());
        } else {
            return new MailConfig();
        }
    }

    public String getSmtpName() {
        return smtpName;
    }

    public MailClient getMailClient() {
        return mailClient;
    }

    public void close() {
        if (null != mailClient) {
            mailClient.close();
            System.out.println("KeelSmtpKit closed client " + this.smtpName);
        }
    }

    public Future<MailResult> quickSendTextMail(
            List<String> receivers,
            String subject,
            String textContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setText(textContent);

        return this.mailClient.sendMail(message);
    }

    public Future<MailResult> quickSendHtmlMail(
            List<String> receivers,
            String subject,
            String htmlContent
    ) {
        MailMessage message = new MailMessage();
        message.setFrom(this.mailConfig.getUsername());
        message.setTo(receivers);
        message.setSubject(subject);
        message.setHtml(htmlContent);

        return this.mailClient.sendMail(message);
    }
}
