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
 * @since 2.7 rename back to KeelProgram from KeelProgramAsVerticle
 */
public abstract class KeelProgram extends KeelVerticle {

    public static void runProgramAndExit(KeelProgram programVerticle, List<String> args) {
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

    abstract protected List<Option> defineCLIOptions();

    @Override
    public final void start() throws Exception {
        super.start();

        Keel.registerDeployedKeelVerticle(this);

        setLogger(prepareLogger());
        execute()
                .compose(v -> {
                    getLogger().notice("DONE");
                    return Future.succeededFuture();
                })
                .recover(throwable -> {
                    getLogger().exception("FAILED", throwable);
                    return Future.succeededFuture();
                })
                .eventually(v -> undeployMe()
                        .eventually(vv -> Keel.getVertx().close()));
    }

    /**
     * 根据 传入的 config 进行业务运作。
     *
     * @return future
     */
    abstract protected Future<Void> execute();

    @Override
    public void stop() throws Exception {
        super.stop();
        Keel.unregisterDeployedKeelVerticle(this.deploymentID());
    }
}
