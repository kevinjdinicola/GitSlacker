package com.dini.GitSlacker;

import com.dini.GitSlacker.config.GitSlackerConfiguration;
import com.dini.GitSlacker.services.*;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import io.dropwizard.setup.Environment;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by kevin on 9/13/16.
 */
public class MainModule extends AbstractModule{

    Logger log = LoggerFactory.getLogger(MainModule.class);

    GitSlackerConfiguration configuration;
    Environment environment;

    public MainModule(GitSlackerConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {

        EventBus gitMessagingBus = new EventBus();

        bind(EventBus.class).toInstance(gitMessagingBus);
        bind(GitSlackerConfiguration.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);

        try {
            GitHub github = GitHub.connectUsingPassword(configuration.getGithubUsername(),configuration.getGithubPassword());
            bind(GitHub.class).toInstance(github);

            github.getMyself();

        } catch (IOException e) {
            log.error(e.toString());
            throw new Error("\"Error, cannot start GitSlacker because there was a problem with the credentials you provided for GitHub access.\"");
        }


        try {
            SlackSession session = SlackSessionFactory.createWebSocketSlackSession(configuration.getSlackApiKey());
              session.connect();
              bind(SlackSession.class).toInstance(session);
        } catch (IOException e) {
            log.error(e.toString());
            throw new Error("Error, cannot start GitSlacker because there was a problem with the credentials you provided for Slack access.");
        }


        bind(SlackService.class).to(SlackServiceImpl.class).asEagerSingleton();
        bind(GitHubEventService.class).to(GitHubEventServiceImpl.class).asEagerSingleton();
        bind(UserService.class).to(UserServiceImpl.class).asEagerSingleton();
    }
}
