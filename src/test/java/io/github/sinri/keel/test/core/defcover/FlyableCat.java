package io.github.sinri.keel.test.core.defcover;

import java.util.UUID;

/**
 * as KeelVerticle
 */
abstract public class FlyableCat extends Pet implements FlyableAnimal {
    private String dyingMessage;

    public FlyableCat(String name) {
        super(name);
        this.dyingMessage = "还没死";
    }

    @Override
    public void die() {
        this.dyingMessage = UUID.randomUUID().toString();
    }

    @Override
    public String getDyingMessage() {
        return this.dyingMessage;
    }

    @Override
    public boolean isLegal() {
        return false;
    }
}
