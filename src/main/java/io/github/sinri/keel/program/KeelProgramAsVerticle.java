package io.github.sinri.keel.program;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * @since 2.0
 */
public abstract class KeelProgramAsVerticle extends KeelVerticle {

    private KeelLogger logger;

    public static void runProgramAndExit(KeelProgramAsVerticle programVerticle, List<String> args) {
        List<Option> options = programVerticle.defineCLIOptions();
        CLI cli = CLI.create(programVerticle.getClass().getName());
        for (var option : options) {
            cli.addOption(option);
        }

        CommandLine parsed = cli.parse(args);

        JsonObject optionMap = new JsonObject();
        for (var option : options) {
            String optionValue = parsed.getOptionValue(option.getLongName());
            optionMap.put(option.getLongName(), optionValue);
        }

        programVerticle.deployMeAsWorker(new DeploymentOptions().setConfig(optionMap));
    }

    abstract protected KeelLogger prepareLogger();

    public KeelLogger getLogger() {
        return logger;
    }

    abstract protected List<Option> defineCLIOptions();

    @Override
    public final void start() throws Exception {
        super.start();
        logger = prepareLogger();
        execute()
                .onComplete(asyncResult -> {
                    if (asyncResult.succeeded()) {
                        // done
                        getLogger().notice("DONE");
                    } else {
                        // failed
                        getLogger().exception("FAILED", asyncResult.cause());
                    }
                    undeployMe().eventually(v -> Keel.getVertx().close());
                });
    }

    /**
     * 根据 传入的 config 进行业务运作。
     *
     * @return future
     */
    abstract protected Future<Void> execute();
}
