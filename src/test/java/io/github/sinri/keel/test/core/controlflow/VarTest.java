package io.github.sinri.keel.test.core.controlflow;

public class VarTest {
    public static void main(String[] args) {
        VarTest.x1("a", "b", "c");

        String[] strings = new String[3];
        strings[0] = "d";
        strings[1] = "e";
        strings[2] = "f";
        VarTest.x2(strings);
    }

    public static void x1(String... vars) {
        for (var v : vars) {
            System.out.println("x1: " + v);
        }
    }

    public static void x2(String[] vars) {
        for (var v : vars) {
            System.out.println("x2: " + v);
        }
    }
}
