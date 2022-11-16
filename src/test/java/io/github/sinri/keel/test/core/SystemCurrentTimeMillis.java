package io.github.sinri.keel.test.core;

import java.util.Date;

public class SystemCurrentTimeMillis {
    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        System.out.println(l);
        System.out.println(new Date().getTime());
    }
}
