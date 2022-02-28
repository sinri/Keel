package io.github.sinri.keel.program;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

/**
 * @since 1.5
 */
abstract public class KeelProgram {
    private final KeelLogger logger;
    private final HashMap<String, String> optionMap;

    public KeelProgram() {
        logger = generateLogger();
        optionMap = new HashMap<>();
    }

    /**
     * This is to be call from JAVA MAIN entrance, to call the target program from CLI.
     *
     * @param args             generated from `String[] args` as a list
     * @param programNamespace the base namespace of all the programs, e.g. a.b.c
     * @param program          the class, with sub namespace as prefix if needed
     * @param action           the action name, to determine what to do exactly
     */
    public static void callFromJavaMain(List<String> args, String programNamespace, String program, String action) {
        String class_full_name = programNamespace + (programNamespace.isEmpty() ? "" : ".") + program;
        Keel.outputLogger(KeelProgram.class.getName()).debug("CLI PROGRAM CLASS: " + class_full_name + " METHOD: " + action);
        try {
            Class<?> handlerClass = Class.forName(class_full_name);
            KeelProgram programClassInstance = (KeelProgram) handlerClass.getDeclaredConstructor().newInstance();
            programClassInstance.executeFromCLI(args, action);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            Keel.outputLogger(KeelProgram.class.getName()).exception(e);
        }
    }

    protected KeelLogger generateLogger() {
        return Keel.logger("program/" + getClass().getSimpleName());
    }

    protected KeelLogger getLogger() {
        return logger;
    }

    protected String getOptionValue(String name) {
        return optionMap.get(name);
    }

    abstract protected List<Option> defineCLIOptions(String action);

    /**
     * @param args   arguments as list
     * @param action the action
     * @since 1.9 default as block mode
     */
    public final void executeFromCLI(List<String> args, String action) {
        executeFromCLI(args, action, true);
    }

    public final void executeFromCLI(List<String> args, String action, boolean runBlocked) {
        List<Option> options = defineCLIOptions(action);
        CLI cli = CLI.create(getClass().getName());
        for (var option : options) {
            cli.addOption(option);
        }

        CommandLine parsed = cli.parse(args);

        for (var option : options) {
            String optionValue = parsed.getOptionValue(option.getLongName());
            optionMap.put(option.getLongName(), optionValue);
        }

        JsonObject x = new JsonObject().put("action", action);
        JsonObject y = new JsonObject();
        optionMap.forEach(y::put);
        x.put("options", y);
        getLogger().info(getClass() + " executeFromCLI starting", x);

        if (runBlocked) {
            blockedExecute(action);
        } else {
            execute(action).eventually(v -> Keel.getVertx().close());
        }
    }


    /**
     * @param action the action
     * @return should not be null
     * @deprecated since 1.9 blocked is better?
     */
    @Deprecated
    abstract protected Future<Void> execute(String action);

    /**
     * @param action the action
     * @since 1.9
     */
    abstract protected void blockedExecute(String action);
}
