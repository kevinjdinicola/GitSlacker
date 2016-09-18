package com.dini.GitSlacker.services;

import io.dropwizard.lifecycle.Managed;

/**
 * Created by kevin on 9/13/16.
 */
public interface GitHubEventService extends Managed {

    void registerRepository(String repositoryName);

}
