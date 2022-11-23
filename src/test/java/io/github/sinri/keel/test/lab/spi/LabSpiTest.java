package io.github.sinri.keel.test.lab.spi;

import io.github.sinri.keel.lab.spi.LabSpiInterface;

import java.util.ServiceLoader;

public class LabSpiTest {
    public static void main(String[] args) {
        ServiceLoader<LabSpiInterface> serviceLoader = ServiceLoader.load(LabSpiInterface.class);
        for (LabSpiInterface service : serviceLoader) {
            service.execute();
        }

    }
}
