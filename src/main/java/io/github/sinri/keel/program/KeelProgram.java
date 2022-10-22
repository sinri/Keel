package io.github.sinri.keel.program;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 2.0
 * @since 2.7 Rename back to KeelProgram from KeelProgramAsVerticle.
 * @since 2.9 Greatly changed. No longer using Verticle with vertx cmd usage.
 */
public abstract class KeelProgram {
    private KeelLogger logger;
    private JsonObject optionMap;

    public KeelProgram() {

    }

    public static void runProgramAndExit(KeelProgram program, List<String> args) {
        List<Option> options = program.defineCLIOptions();
        CLI cli = CLI.create(program.getClass().getName());
        for (var option : options) {
            cli.addOption(option);
        }

        CommandLine parsed = cli.parse(args);

        program.optionMap = new JsonObject();
        for (var option : options) {
            Object optionValue = parsed.getOptionValue(option.getName());
            program.optionMap.put(option.getName(), optionValue);
        }
        program.run();
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public JsonObject config() {
        return optionMap;
    }

    abstract protected KeelLogger prepareLogger();

    abstract protected List<Option> defineCLIOptions();

    public final void run() {
        this.logger = prepareLogger();
        AtomicInteger returnCode = new AtomicInteger(0);
        Future.succeededFuture()
                .compose(v -> execute())
                .onSuccess(v -> {
                    getLogger().notice("DONE");
                })
                .onFailure(throwable -> {
                    getLogger().exception("FAILED", throwable);
                    returnCode.set(generateReturnCode(throwable));
                })
                .eventually(v -> Keel.getVertx().close())
                .onComplete(v -> {
                    System.exit(returnCode.get());
                });
    }

    /**
     * 根据 传入的 config 进行业务运作。
     *
     * @return future
     */
    abstract protected Future<Void> execute();

    protected int generateReturnCode(Throwable throwable) {
        return 0;
    }
}
