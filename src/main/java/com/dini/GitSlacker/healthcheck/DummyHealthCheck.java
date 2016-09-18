package com.dini.GitSlacker.healthcheck;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by kevin on 9/13/16.
 */
public class DummyHealthCheck extends HealthCheck {
    protected Result check() throws Exception {
        return Result.healthy("Dummy healthcheck says you're good.");
    }
}
