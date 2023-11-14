package io.github.sinri.keel.test.lab.helper;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpTest {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress inetAddress = Inet4Address.getByName("125.23.256.22");
        System.out.println(inetAddress.getHostAddress());

    }
}
