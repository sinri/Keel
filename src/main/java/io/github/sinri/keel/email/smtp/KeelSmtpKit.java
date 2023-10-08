package io.github.sinri.keel.email.smtp;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.KeelConfiguration;
import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;

import java.util.List;
import java.util.Objects;

/**
 * @since 1.10
 * @since 3.0.6 changed a lot
 */
public class KeelSmtpKit {
    private final MailConfig mailConfig;
    private final MailClient mailClient;

    /**
     * @since 3.0.6
     */
    public KeelSmtpKit(MailConfig mailConfig, String poolName) {
        this.mailConfig = mailConfig;
        if (poolName != null) {
            this.mailClient = MailClient.createShared(Keel.getVertx(), this.mailConfig, poolName);
        } else {
            this.mailClient = MailClient.create(Keel.getVertx(), this.mailConfig);
        }
    }

    public KeelSmtpKit(String smtpName, boolean shared) {
        this(buildMailConfig(smtpName), shared ? Objects.requireNonNull(smtpName) : null);
    }

    public KeelSmtpKit(String smtpName) {
        this(smtpName, true);
    }

    public KeelSmtpKit() {
        this(
                Objects.requireNonNull(
                        Keel.getConfiguration().readString("email", "smtp", "default_smtp_name"),
                        "email.smtp.default_smtp_name is not configured"
                )
        );
    }

    /**
     * As of 3.0.6, only five property keys supported.
     */
    private static MailConfig buildMailConfig(String smtpName) {
        KeelConfiguration smtpConfiguration = Keel.getConfiguration().extract("email", "smtp", smtpName);

        var mailConfig = new MailConfig();
        mailConfig.setHostname(smtpConfiguration.readString("hostname"));
        mailConfig.setPort(Objects.requireNonNull(smtpConfiguration.readAsInteger("port")));
        mailConfig.setUsername(smtpConfiguration.readString("username"));
        mailConfig.setPassword(smtpConfiguration.readString("password"));
        mailConfig.setSsl("ON".equals(smtpConfiguration.readString("ssl")));

        return mailConfig;
    }

    public MailClient getMailClient() {
        return mailClient;
    }

    public Future<Void> close() {
        if (null != mailClient) {
            return mailClient.close();
        }
        return Future.succeededFuture();
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

    /**
     * @since 3.0.6
     */
    public MailConfig getMailConfig() {
        return mailConfig;
    }
}
