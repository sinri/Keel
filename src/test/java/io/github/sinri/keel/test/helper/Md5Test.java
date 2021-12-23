package io.github.sinri.keel.test.helper;

import io.github.sinri.keel.core.KeelHelper;

public class Md5Test {
    public static void main(String[] args) {
        String s = KeelHelper.md5("123喵-喵=喵Abc");
        System.out.println(s);
    }
}
