package io.github.sinri.keel.test.lab.helper;

import io.github.sinri.keel.helper.authenticator.googleauth.GoogleAuthenticatorKey;
import io.github.sinri.keel.helper.authenticator.googleauth.GoogleAuthenticatorQRGenerator;
import io.github.sinri.keel.helper.authenticator.googleauth.async.AsyncGoogleAuthenticator;
import io.github.sinri.keel.helper.authenticator.googleauth.async.AsyncICredentialRepository;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class GoogleTotpTest extends KeelTest {
    private AsyncGoogleAuthenticator asyncGoogleAuthenticator;

    @NotNull
    @Override
    protected Future<Void> starting() {
        asyncGoogleAuthenticator = Keel.authenticationHelper().getAsyncGoogleAuthenticator();
        return Future.succeededFuture();
    }

    @TestUnit(skip = true)
    public Future<Void> createAnonymousKey() {
        GoogleAuthenticatorKey authenticatorKey = asyncGoogleAuthenticator.createCredentials();
        getLogger().info("Created Anonymous Key", j -> j
                .put("key", authenticatorKey.getKey())
                .put("ScratchCodes", new JsonArray(authenticatorKey.getScratchCodes()))
                .put("VerificationCode", authenticatorKey.getVerificationCode())
        );
        return Future.succeededFuture();

        /*
        keel_anonymous_test
         {
         "key":"C4TQUWLWMJ7SB3TY5EIKJIFDEOEHDEHX",
         "ScratchCodes":[32689698,88562402,42793017,77221105,73680861],
         "VerificationCode":120501
         }
         */
    }

    @TestUnit(skip = true)
    public Future<Void> authAnonymousTotp() {
        String key = "C4TQUWLWMJ7SB3TY5EIKJIFDEOEHDEHX";
        int totpPassword = asyncGoogleAuthenticator.getTotpPassword(key);// or copy from Google Authenticator
        return asyncGoogleAuthenticator.authorize(key, totpPassword)
                .compose(ok -> {
                    if (ok) {
                        getLogger().info("AUTH OK");
                        return Future.succeededFuture();
                    } else {
                        getLogger().error("AUTH FAILED");
                        return Future.failedFuture("AUTH FAILED");
                    }
                });
    }

    @TestUnit(skip = true)
    public Future<Void> createNamedKey() {
        asyncGoogleAuthenticator.setCredentialRepository(new CredentialRepositoryImpl());
        return asyncGoogleAuthenticator.createCredentials("testor")
                .compose(authenticatorKey -> {
                    getLogger().info("Created Named Key", j -> j
                            .put("key", authenticatorKey.getKey())
                            .put("ScratchCodes", new JsonArray(authenticatorKey.getScratchCodes()))
                            .put("VerificationCode", authenticatorKey.getVerificationCode())
                    );
                    return Future.succeededFuture();
                });
    }

    @TestUnit(skip = true)
    public Future<Void> authNamedTotp() {
        asyncGoogleAuthenticator.setCredentialRepository(new CredentialRepositoryImpl());
//        String key = "EHC5TIME4XL4ERZBTLWEZOK37KCHDO4J";
//        int totpPassword = asyncGoogleAuthenticator.getTotpPassword(key);// or copy from Google Authenticator
        return asyncGoogleAuthenticator.authorizeUser("testor", 265377)
                .compose(ok -> {
                    if (ok) {
                        getLogger().info("AUTH OK");
                        return Future.succeededFuture();
                    } else {
                        getLogger().error("AUTH FAILED");
                        return Future.failedFuture("AUTH FAILED");
                    }
                });
    }

    @TestUnit(skip = false)
    public Future<Void> qrForNamed() {
        String otpAuthTotpURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "Keel",
                "testor",
                new GoogleAuthenticatorKey.Builder("EHC5TIME4XL4ERZBTLWEZOK37KCHDO4J")
                        .setScratchCodes(List.of(85453085, 24908943, 37457523, 99063161, 21235363))
                        .setVerificationCode(408813)
                        .build()
        );
        getLogger().info("otpAuthTotpURL: " + otpAuthTotpURL);
        return Future.succeededFuture();
    }

    private static class CredentialRepositoryImpl implements AsyncICredentialRepository {
        private final JsonObject dict;

        public CredentialRepositoryImpl() {
            this.dict = new JsonObject();

            // loaded
            this.dict.put("testor", new JsonObject()
                    .put("secretKey", "EHC5TIME4XL4ERZBTLWEZOK37KCHDO4J")
                    .put("validationCode", 408813)
                    .put("scratchCodes", new JsonArray()
                            .add(85453085).add(24908943).add(37457523).add(99063161).add(21235363)
                    )
            );
        }

        /**
         * This method retrieves the Base32-encoded private key of the given user.
         *
         * @param userName the user whose private key shall be retrieved.
         * @return the private key of the specified user.
         */
        @Override
        public Future<String> getSecretKey(String userName) {
            return Future.succeededFuture()
                    .compose(v -> {
                        JsonObject jsonObject = dict.getJsonObject(userName);
                        Keel.getLogger().fatal("for username " + userName, jsonObject);
                        var x = jsonObject.getString("secretKey");
                        return Future.succeededFuture(x);
                    });
        }

        /**
         * This method saves the user credentials.
         *
         * @param userName       the user whose data shall be saved.
         * @param secretKey      the generated key.
         * @param validationCode the validation code.
         * @param scratchCodes   the list of scratch codes.
         */
        @Override
        public Future<Void> saveUserCredentials(String userName, String secretKey, int validationCode, List<Integer> scratchCodes) {
            var x = new JsonObject()
                    .put("secretKey", secretKey)
                    .put("validationCode", validationCode)
                    .put("scratchCodes", new JsonArray(scratchCodes));
            this.dict.put(userName, x);
            Keel.getLogger().fatal("saveUserCredentials for " + userName, x);
            return Future.succeededFuture();

            /*
             * testor
             * {"secretKey":"EHC5TIME4XL4ERZBTLWEZOK37KCHDO4J","validationCode":408813,"scratchCodes":[85453085,24908943,37457523,99063161,21235363]}
             */
        }
    }
}
