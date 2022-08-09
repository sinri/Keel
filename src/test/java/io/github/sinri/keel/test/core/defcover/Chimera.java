package io.github.sinri.keel.test.core.defcover;

public class Chimera extends FlyableCat {
    public Chimera(String name) {
        super(name);
    }

    @Override
    public void fly() {
        System.out.println(getName() + " is flying, it is of type " + type);
    }
}
