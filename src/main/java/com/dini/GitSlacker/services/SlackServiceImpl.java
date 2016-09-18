package com.dini.GitSlacker.services;

import com.dini.GitSlacker.models.GitHubEventMessage;
import com.dini.GitSlacker.models.User;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    LinkedBlockingDeque<QueuedMessage> slackMessageQueue;
    Thread sendMessageThread;

    @Inject
    public SlackServiceImpl(EventBus eventBus, SlackSession slackSession, GitHubEventService gitHubEventService, UserService userService) {
        this.eventBus = eventBus;
        this.slackSession = slackSession;
        this.gitHubEventService = gitHubEventService;
        this.userService = userService;
        slackMessageQueue = new LinkedBlockingDeque<>();
    }

    public void createNotifier(String gitRepo, String channel) {
        eventBus.register(new GitEventSubscriber(gitRepo, channel));
        log.info("Slack service registered subscriber for events on " + gitRepo);

    }

    public void sendMessage(SlackPreparedMessage slackMsg, String channel) {
        log.debug("Queueing message for slack channel " + channel);
        slackMessageQueue.offer(new QueuedMessage(channel, slackMsg));
    }

    @Override
    public void start() throws Exception {
        Runnable sendMessageWorker = () -> {
            while (!Thread.interrupted()) {
                try {
                    QueuedMessage queuedMessage = slackMessageQueue.take();
                    SlackMessageHandle<SlackMessageReply> response = slackSession.sendMessage(
                            slackSession.findChannelByName(queuedMessage.getChannel()),
                            queuedMessage.getSlackMsg());

                    // slack didn't receive our message, queue it again and wait a bit for connectivity
                    if (response.getReply() == null) {
                        slackMessageQueue.addFirst(queuedMessage);
                        Thread.sleep(5000);
                    }

                } catch (Exception e) {
                    // TODO handle logging
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
            try {
                User user = userService.getUserByGitHubLogin(message.getEvent().getActorLogin());
                slackName = user != null ? user.getSlackName() : null;
            } catch (IOException e) {
                log.error("An error occured resolving slack name.");
            }

            SlackPreparedMessage slackMsg = message.generateMessage(slackName);
            if (slackMsg != null) {
                sendMessage(slackMsg,channel);
            } else {
                //TODO logging failed to print message
            }
        }
    }
}
