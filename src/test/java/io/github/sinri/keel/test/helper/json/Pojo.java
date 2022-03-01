package io.github.sinri.keel.test.helper.json;

public class Pojo {
    private final String privateField;
    public String publicField;
    protected String protectedField;

    public Pojo(String a, String b, String c) {
        this.publicField = a;
        this.protectedField = b;
        this.privateField = c;
    }
}
