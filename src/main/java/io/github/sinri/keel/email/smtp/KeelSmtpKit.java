package io.github.sinri.keel.email.smtp;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.core.Future;
import io.vertx.ext.mail.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @since 1.10
 */
public class KeelSmtpKit {
    private final String smtpName;
    private final MailConfig mailConfig;
    private final MailClient mailClient;

    public KeelSmtpKit(String smtpName, boolean shared) {
        this.smtpName = smtpName;
        this.mailConfig = buildMailConfig(smtpName);
        if (shared) {
            this.mailClient = MailClient.createShared(Keel.getVertx(), this.mailConfig, smtpName);
        } else {
            this.mailClient = MailClient.create(Keel.getVertx(), this.mailConfig);
        }
    }

    public KeelSmtpKit(String smtpName) {
        this.smtpName = smtpName;
        this.mailConfig = buildMailConfig(smtpName);
        this.mailClient = MailClient.create(Keel.getVertx(), this.mailConfig);
    }

    public KeelSmtpKit() {
        this.smtpName = Keel.getPropertiesReader().getProperty("email.smtp.default_name");
        this.mailConfig = buildMailConfig(smtpName);
        this.mailClient = MailClient.create(Keel.getVertx(), this.mailConfig);
    }

    private static MailConfig buildMailConfig(String smtpName) {
        KeelPropertiesReader propertiesReader = Keel.getPropertiesReader();
        String prefix = "email.smtp." + smtpName;

        MailConfig mailConfig = new MailConfig();

        KeelPropertiesReader filtered = propertiesReader.filter(prefix);
        for (var key : filtered.getPlainKeySet()) {
            var value = filtered.getProperty(key);
            switch (key) {
                case "hostname":
                    mailConfig.setHostname(value);
                    break;
                case "port":
                    mailConfig.setPort(Integer.parseInt(value));
                    break;
                case "startTLS":
                    mailConfig.setStarttls(StartTLSOptions.valueOf(value));
                    break;
                case "login":
                    mailConfig.setLogin(LoginOption.valueOf(value));
                    break;
                case "username":
                    mailConfig.setUsername(value);
                    break;
                case "password":
                    mailConfig.setPassword(value);
                    break;
                case "ssl":
                    mailConfig.setSsl("ON".equalsIgnoreCase(value));
                    break;
                case "authMethods":
                    mailConfig.setAuthMethods(value);
                    break;
                case "keepAlive":
                    mailConfig.setKeepAlive("ON".equalsIgnoreCase(value));
                    break;
                case "maxPoolSize":
                    mailConfig.setMaxPoolSize(Integer.parseInt(value));
                    break;
                case "trustAll":
                    mailConfig.setTrustAll("ON".equalsIgnoreCase(value));
                    break;
                case "allowRcptErrors":
                    mailConfig.setAllowRcptErrors("ON".equalsIgnoreCase(value));
                    break;
                case "userAgent":
                    mailConfig.setUserAgent(value);
                    break;
                case "multiPartOnly":
                    mailConfig.setMultiPartOnly("ON".equalsIgnoreCase(value));
                    break;
                case "poolCleanerPeriod":
                    mailConfig.setPoolCleanerPeriod(Integer.parseInt(value));
                    break;
                case "poolCleanerPeriodUnit":
                    mailConfig.setPoolCleanerPeriodUnit(TimeUnit.valueOf(value));
                    break;
                case "keepAliveTimeout":
                    mailConfig.setKeepAliveTimeout(Integer.parseInt(value));
                    break;
                case "keepAliveTimeoutUnit":
                    mailConfig.setKeepAliveTimeoutUnit(TimeUnit.valueOf(value));
                    break;
            }
        }

        return mailConfig;
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
            Keel.outputLogger("smtp").notice("KeelSmtpKit closed client " + this.smtpName);
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
