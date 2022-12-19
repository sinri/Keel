package io.github.sinri.keel.facade;

import io.github.sinri.keel.facade.async.KeelTraitForVertxAsync;
import io.github.sinri.keel.facade.interfaces.KeelTraitForClusteredVertx;
import io.github.sinri.keel.facade.interfaces.KeelTraitForVertx;
import io.github.sinri.keel.helper.TraitForHelpers;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.mysql.MySQLDataSourceProvider;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Keel extends KeelTraitForVertx, KeelTraitForClusteredVertx, KeelTraitForVertxAsync, TraitForHelpers, MySQLDataSourceProvider {

    //Keel instance = new KeelImpl();

//    static Keel getInstance() {
//        return instance;
//    }

    /**
     * In the beginning ...
     */
    static Future<Keel> genesis(Function<Keel, Future<Void>> initializer) {
        Keel keel = new KeelImpl();
        return initializer.apply(keel)
                .compose(v -> {
                    if (!keel.isVertxInitialized()) {
                        throw new NullPointerException("KEEL NOT INITIALIZED");
                    }
                    return Future.succeededFuture(keel);
                });
    }

    KeelConfiguration getConfiguration();

    default Future<Void> loadConfigureWithPropertiesFile(String propertiesFile) {
        getConfiguration().loadPropertiesFile(propertiesFile);
        return Future.succeededFuture();
    }

    default Future<String> deployKeelVerticle(@Nonnull KeelVerticle keelVerticle, DeploymentOptions deploymentOptions) {
        // single instance
        keelVerticle.setKeel(this);
        return getVertx().deployVerticle(keelVerticle, deploymentOptions);
    }

    default Future<String> deployKeelVerticle(Class<? extends KeelVerticle> keelVerticleClass, DeploymentOptions deploymentOptions) {
        return getVertx().deployVerticle(() -> {
            try {
                KeelVerticle keelVerticle = keelVerticleClass.getConstructor().newInstance();
                keelVerticle.setKeel(this);
                return keelVerticle;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }, deploymentOptions);
    }

    default Future<String> deployKeelVerticle(Supplier<KeelVerticle> keelVerticleSupplier, DeploymentOptions deploymentOptions) {
        Supplier<Verticle> verticleSupplier = () -> {
            KeelVerticle keelVerticle = keelVerticleSupplier.get();
            keelVerticle.setKeel(this);
            return keelVerticle;
        };
        return getVertx().deployVerticle(verticleSupplier, deploymentOptions);
    }

    KeelEventLogger getInstantEventLogger();

    KeelEventLogger createOutputEventLogger(String topic);
}
