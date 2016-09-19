package com.dini.GitSlacker.services;

import com.dini.GitSlacker.messages.GitHubEventMessage;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin on 9/13/16.
 */
public class GitHubEventServiceImpl implements GitHubEventService {

    Logger log = LoggerFactory.getLogger(GitHubEventServiceImpl.class);

    EventBus eventBus;
    GitHub github;
    ScheduledExecutorService executor;

    ArrayList<RepositoryCheckTask> repositoriesToCheck;

    @Inject
    public GitHubEventServiceImpl(EventBus eventBus, GitHub github, Environment environment) {
        this.eventBus = eventBus;
        this.github = github;

        repositoriesToCheck = new ArrayList<>();

        executor = environment.lifecycle().scheduledExecutorService("github-event-service")
                .threads(1)
                .build();
    }

    @Override
    public void registerRepository(String repositoryName)  {
        repositoriesToCheck.add(new RepositoryCheckTask(repositoryName));
    }

    @Override
    public void start() throws Exception {
        executor.scheduleAtFixedRate(() -> repositoriesToCheck.forEach(x -> {
            try {
                checkRepositoryForEvents(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }), 0, 10, TimeUnit.SECONDS);
    }

    private void checkRepositoryForEvents(RepositoryCheckTask repo) throws IOException {
        log.debug("Checking repo " + repo + " for events after " + repo.getMostRecentEventDate().toString());
        GHRepository ghRepo = github.getRepository(repo.getRepositoryName());
        boolean foundAllNewEvents = false;
        PagedIterator<GHEventInfo> itr = ghRepo.listEvents().iterator();

        // we only want events that are after the most recent event we've received
        Date nextNewestDate = repo.getMostRecentEventDate();

        // messages will be read in latested->oldest order
        // lets stick them in a stack so when we post them
        // to the event bus they will be in oldest->latest
        // order.  i.e. the order they occured
        Stack<GitHubEventMessage> messagesToPost = new Stack<>();

        while (!foundAllNewEvents && itr.hasNext()) {
            GHEventInfo event = itr.next();
            // is this event newer than the date i have for the latest event?
            if (event.getCreatedAt().compareTo(repo.getMostRecentEventDate()) > 0) {
                log.debug("Found new event id: " + event.getId());
                GitHubEventMessage message = new GitHubEventMessage(repo.getRepositoryName(), event);
                messagesToPost.add(message);

                // the event we just discovered is more recent
                // that what we have stored as the most recent event
                // weve encountered
                if (event.getCreatedAt().after(nextNewestDate)) {
                    nextNewestDate = event.getCreatedAt();
                }

            } else {
                foundAllNewEvents = true;
                log.debug("Finished searching for events");
            }
        }

        while (messagesToPost.size() > 0) {
            log.debug("Posting " + messagesToPost.size() + " new events to event bus");
            eventBus.post(messagesToPost.pop());
        }

        repo.setMostRecentEventDate(nextNewestDate);
    }

    @Override
    public void stop() throws Exception {
        executor.shutdown();
    }

    @Getter
    @Setter
    class RepositoryCheckTask {
        String repositoryName;
        Date mostRecentEventDate;

        public RepositoryCheckTask(String repositoryName) {
            this.repositoryName = repositoryName;
            mostRecentEventDate = new Date(new Date().getTime());
        }

    }
}
