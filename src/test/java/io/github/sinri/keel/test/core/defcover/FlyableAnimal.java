package io.github.sinri.keel.test.core.defcover;

/**
 * as UndeployableVertical
 */
public interface FlyableAnimal extends Animal {
    void fly();

    String getName();

    String getDyingMessage();

    default void showDyingMessage() {
        System.out.println(getName() + ":" + getDyingMessage());
    }
}
