package com.dini.GitSlacker.services;

import com.dini.GitSlacker.models.GitHubEventMessage;
import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.dini.GitSlacker.models.User;
import com.dini.GitSlacker.responders.SlackMessageResponder;
import com.dini.GitSlacker.responders.SlackMessageResponseGenerator;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by kevin on 9/13/16.
 */
public class SlackServiceImpl implements SlackService {

    Logger log = LoggerFactory.getLogger(SlackServiceImpl.class);

    EventBus eventBus;
    SlackSession slackSession;
    GitHubEventService gitHubEventService;
    UserService userService;
    GitHub github;

    LinkedBlockingDeque<QueuedMessage> slackMessageQueue;
    Thread sendMessageThread;

    HashMap<String, String> contextMap;

    @Inject
    public SlackServiceImpl(
            EventBus eventBus, SlackSession slackSession, GitHubEventService gitHubEventService,
            UserService userService, GitHub github) {
        this.eventBus = eventBus;
        this.slackSession = slackSession;
        this.gitHubEventService = gitHubEventService;
        this.userService = userService;
        this.github = github;

        slackMessageQueue = new LinkedBlockingDeque<>();
        contextMap = new HashMap<>();
    }

    public void createNotifier(String gitRepo, String channel) {
        eventBus.register(new GitEventSubscriber(gitRepo, channel));
        slackSession.joinChannel(channel);
        log.info("Slack service registered subscriber for events on " + gitRepo);

    }

    @Override
    public void addResponder(SlackMessageResponseGenerator responseGenerator) {
        SlackMessageResponder newResponder = new SlackMessageResponder(this, github, responseGenerator);
        slackSession.addMessagePostedListener(newResponder);
    }


    public void sendMessage(SlackPreparedMessage slackMsg, String channel) {
        log.debug("Queueing message for slack channel " + channel);
        slackMessageQueue.offer(new QueuedMessage(channel, slackMsg));
    }

    public HashMap<String, String> getContextMap() {
        return contextMap;
    }


    @Override
    public void start() throws Exception {
        setupSlackMessageQueue();
    }

    private void updateMessageContextCache(HashMap<String, String> latestContextMap) {
        latestContextMap.putAll(latestContextMap);
    }

    public void sendMessage(SlackMessageTemplate message, String channel) {
        updateMessageContextCache(message.getLatestContextVariables());
        sendMessage(message.generateMessage(), channel);
    }

    private void setupSlackMessageQueue() {
        Runnable sendMessageWorker = () -> {
            while (!Thread.interrupted()) {
                try {
                    QueuedMessage queuedMessage = slackMessageQueue.take();
                    SlackMessageHandle<SlackMessageReply> response = slackSession.sendMessage(
                            slackSession.findChannelByName(queuedMessage.getChannel()),
                            queuedMessage.getSlackMsg());

                    // slack didn't receive our message, queue it again and wait a bit for connectivity
                    if (response.getReply() == null) {
                        log.error("Failed to send slack message, re-adding to front of queue and retrying in 5 seconds...");
                        slackMessageQueue.addFirst(queuedMessage);
                        Thread.sleep(5000);
                    }

                    // let the slack api rest a little...
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("An error occurred while sending a message to slack");
                    log.error(e.toString());
                }
            }
        };

        sendMessageThread = new Thread(sendMessageWorker);
        sendMessageThread.start();
    }

    @Override
    public void stop() throws Exception {
        sendMessageThread.interrupt();
    }

    @Getter
    @Setter
    class QueuedMessage {
        String channel;
        SlackPreparedMessage slackMsg;

        public QueuedMessage(String channel, SlackPreparedMessage slackMsg) {
            this.channel = channel;
            this.slackMsg = slackMsg;
        }
    }

    @Getter
    @Setter
    /* A class the represents a subscriber that
     * watches an event bus for messages about
     * a particular repository and then
     * posts them to a specific channel on slack
     * */
    public class GitEventSubscriber {

        String repository;
        String channel;

        public GitEventSubscriber(String repository, String channel) {
            this.repository = repository;
            this.channel = channel;
            slackSession.joinChannel(channel);
        }

        @Subscribe
        public void receiveEvent(GitHubEventMessage message) {

            // ignore all messages that weren't destined for this subscriber
            if (!message.getRepositoryName().equals(this.repository)) {
                return;
            }

            log.info("Subscriber received github event for " + message+ ", posting to slack.");

            String slackName = null;

            HashMap<String, String> overrideArguments = new HashMap<>();
            try {
                User user = userService.getUserByGitHubLogin(message.getEvent().getActorLogin());
                if (user != null) {
                    overrideArguments.put("actor", "@"+user.getSlackName());
                }
            } catch (IOException e) {
                log.error("An error occured resolving slack name.");
            }

            updateMessageContextCache(message.getLatestContextVariables());

            SlackPreparedMessage slackMsg = message.generateMessage(overrideArguments);
            sendMessage(slackMsg,channel);
        }
    }
}
