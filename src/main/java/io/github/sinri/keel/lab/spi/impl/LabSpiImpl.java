package io.github.sinri.keel.lab.spi.impl;

import io.github.sinri.keel.lab.spi.LabSpiInterface;

public class LabSpiImpl implements LabSpiInterface {
    @Override
    public void execute() {
        System.out.println(getClass().getName());
    }
}
