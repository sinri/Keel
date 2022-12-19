package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.core.KeelCronExpression;

import java.util.Calendar;

/**
 * @since 3.0.0
 */
public interface KeelSundialPlan {
    String key();

    KeelCronExpression cronExpression();

    void execute(Calendar now);
}
