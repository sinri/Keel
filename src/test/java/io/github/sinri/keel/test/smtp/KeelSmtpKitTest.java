package io.github.sinri.keel.test.smtp;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.email.smtp.KeelSmtpKit;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.List;

public class KeelSmtpKitTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        var smtp = new KeelSmtpKit();
        smtp.quickSendTextMail(
                        List.of("ljni@leqee.com"),
                        "KeelSmtpKitTest",
                        "Once the client object is created, you can use it to send mails. Since the sending of the mails works asynchronous in vert.x, the result handler will be called when the mail operation finishes. You can start many mail send operations in parallel, the connection pool will limit the number of concurrent operations so that new operations will wait in queue if no slots are available."
                ).onSuccess(mailResult -> {
                    Keel.outputLogger("smtp").notice("MSG ID: " + mailResult.getMessageID());
                    Keel.outputLogger("smtp").notice("getRecipients as following:");
                    for (var x : mailResult.getRecipients()) {
                        Keel.outputLogger("smtp").notice("Recipient: " + x);
                    }
                })
                .onFailure(throwable -> {
                    Keel.outputLogger("smtp").error("sending failed");
                    Keel.outputLogger("smtp").exception(throwable);
                })
                .eventually(v -> {
                    smtp.close();
                    Keel.getVertx().close();
                    return Future.succeededFuture();
                });


    }
}
