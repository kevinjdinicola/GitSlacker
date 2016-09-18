package com.dini.GitSlacker;

import com.dini.GitSlacker.config.GitSlackerConfiguration;
import com.dini.GitSlacker.services.*;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import io.dropwizard.setup.Environment;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Created by kevin on 9/13/16.
 */
public class MainModule extends AbstractModule{

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

            SlackSession session = SlackSessionFactory.createWebSocketSlackSession(configuration.getSlackApiKey());
            session.connect();

            bind(SlackSession.class).toInstance(session);

        } catch (IOException e) {
            e.printStackTrace();
        }


        bind(SlackService.class).to(SlackServiceImpl.class).asEagerSingleton();
        bind(GitHubEventService.class).to(GitHubEventServiceImpl.class).asEagerSingleton();
        bind(UserService.class).to(UserServiceImpl.class).asEagerSingleton();
    }
}
