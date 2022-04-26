package io.github.sinri.keel.test.web;

import io.github.sinri.keel.web.KeelWebRequestReceptionist;
import org.reflections.Reflections;

import java.util.Set;

public class TestGetApiList {
    public static void main(String[] args) {
        Reflections reflections = new Reflections("io.github.sinri.keel.test.web");//.receptionist");

        Set<Class<? extends KeelWebRequestReceptionist>> allClasses = reflections.getSubTypesOf(KeelWebRequestReceptionist.class);
        allClasses.forEach(c -> {
            System.out.println("class " + c.getName());
        });
    }
}
