package com.dini.GitSlacker.responders;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.dini.GitSlacker.services.GitHubEventService;
import com.dini.GitSlacker.services.SlackService;
import com.google.inject.Inject;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import lombok.Getter;
import org.kohsuke.github.GitHub;

/**
 * Created by kevin on 9/18/16.
 */
public class SlackMessageResponder implements SlackMessagePostedListener  {

    @Getter
    protected SlackService slackService;
    @Getter
    protected GitHub gitHub;

    protected SlackMessageResponseGenerator generator;

    public <T extends SlackMessageResponseGenerator> SlackMessageResponder(
            SlackService slackService, GitHub gitHub, SlackMessageResponseGenerator generator) {
        this.slackService = slackService;
        this.gitHub = gitHub;
        this.generator = generator;

    }


    @Override
    public void onEvent(SlackMessagePosted slackMessagePosted, SlackSession slackSession) {
        if (!generator.shouldRespond(slackMessagePosted)) {
            return;
        }

        // create an instance of a message template which can, once provided arguments, generate a message
        SlackMessageTemplate message = generator.generateResponse(this, slackMessagePosted);

        slackService.sendMessage(message, slackMessagePosted.getChannel().getName());
    }
}
