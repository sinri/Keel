package io.github.sinri.keel.test.lab.helper;


import java.net.UnknownHostException;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class IpTest {
    public static void main(String[] args) throws UnknownHostException {
//        InetAddress inetAddress = Inet4Address.getByName("125.23.256.22");
//        System.out.println(inetAddress.getHostAddress());

        try {
            var s = "for dp&bi, when value < 3 and age >4 阿嚏&#64;！！！@飞飞飞";
            System.out.println(KeelHelpers.stringHelper().escapeForHttpEntity(s));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
