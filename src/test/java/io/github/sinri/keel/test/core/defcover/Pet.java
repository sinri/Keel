package io.github.sinri.keel.test.core.defcover;

/**
 * as AbstractVerticle
 */
abstract public class Pet implements Animal {
    private final String name;
    protected String type;

    public Pet(String name) {
        this.name = name;
        this.type = getClass().getName();
    }

    public String getName() {
        return this.name;
    }

    abstract public boolean isLegal();
}
