# AES in Keel

Java provided `PKCS5Padding`.

If you needs `PKCS7Padding`, you may need BC-PROV.

Append this to the pom file.

````xml
<!-- pom.xml dependencies -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.70</version>
</dependency>
````

Then call `io.github.sinri.keel.core.helper.encryption.aes.KeelAesUsingPkcs7Padding.requireBouncyCastleProvider();`
in the initialization code of your program.