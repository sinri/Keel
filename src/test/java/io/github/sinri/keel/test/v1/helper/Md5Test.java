package io.github.sinri.keel.test.v1.helper;

import io.github.sinri.keel.Keel;

public class Md5Test {
    public static void main(String[] args) {
        String s = Keel.helpers().digest().md5("123喵-喵=喵Abc");
        System.out.println(s);
    }
}
