package io.github.sinri.keel.test.program;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.github.sinri.keel.program.KeelProgramAsVerticle;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;
import io.vertx.core.cli.Option;

import java.util.ArrayList;
import java.util.List;

public class ProgramA extends KeelProgramAsVerticle {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        ProgramA programA = new ProgramA();
        KeelProgramAsVerticle.runProgramAndExit(programA, List.of(args));
    }

    @Override
    protected KeelLogger prepareLogger() {
        return Keel.outputLogger("ProgramA");
    }

    @Override
    protected List<Option> defineCLIOptions() {
        List<Option> list = new ArrayList<>();
        list.add(new Option()
                .setLongName("field1"));
        list.add(new Option()
                .setLongName("field2"));
        return list;
    }

    @Override
    protected Future<Void> execute() {
        String field1 = this.config().getString("field1");
        String field2 = this.config().getString("field2");
        getLogger().info("field1=" + field1 + " field2=" + field2);
        return Future.succeededFuture();
    }
}
