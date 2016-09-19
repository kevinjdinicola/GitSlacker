package com.dini.GitSlacker;

import com.dini.GitSlacker.config.GitSlackerConfiguration;
import com.dini.GitSlacker.healthcheck.DummyHealthCheck;
import com.dini.GitSlacker.models.CommitInfoMessage;
import com.dini.GitSlacker.responders.CommitInfoResponder;
import com.dini.GitSlacker.services.GitHubEventService;
import com.dini.GitSlacker.services.SlackService;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import java.io.IOException;

/**
 * Created by kevin on 9/13/16.
 */
public class GitSlackerApplication extends Application<GitSlackerConfiguration> {


    public static void main(String[] args) throws Exception {
        new GitSlackerApplication().run(args);
    }

    @Override
    public String getName() {
        return "GitSlacker";
    }


    @Override
    public void run(GitSlackerConfiguration configuration,
                    Environment environment) {

        // dropwizard requres at least 1 health check
        environment.healthChecks().register("dummy-healthcheck", new DummyHealthCheck());

        Injector ij = Guice.createInjector(new MainModule(configuration, environment));

        GitHubEventService gitHubEventService = ij.getInstance(GitHubEventService.class);
        SlackService slackService = ij.getInstance(SlackService.class);

        // these services have processing threads running that should be managed by the application
        // (started and stopped appropriately)
        environment.lifecycle().manage(gitHubEventService);
        environment.lifecycle().manage(slackService);

        configuration.getWatchedRepositories().forEach(wRepo -> {
            slackService.createNotifier(wRepo.getRepositoryName(),wRepo.getChannelName());
            gitHubEventService.registerRepository(wRepo.getRepositoryName());
        });

        // register responders
        CommitInfoResponder ciResponder = new CommitInfoResponder();
        slackService.addResponder(ciResponder);

    }
}
