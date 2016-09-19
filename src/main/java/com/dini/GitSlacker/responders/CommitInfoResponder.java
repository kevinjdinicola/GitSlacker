package com.dini.GitSlacker.responders;

import com.dini.GitSlacker.messages.CommitInfoMessage;
import com.dini.GitSlacker.messages.SlackErrorMessage;
import com.dini.GitSlacker.messages.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */
public class CommitInfoResponder implements SlackMessageResponseGenerator {

    @Override
    public boolean shouldRespond(SlackMessagePosted postedMessage) {
        String content = postedMessage.getMessageContent().toLowerCase();
        int whatIndex = content.indexOf("what");
        int changedIndex = content.indexOf("changed");

        if (whatIndex >= 0 && changedIndex > whatIndex) {
            return true;
        }

        return false;
    }

    @Override
    public SlackMessageTemplate generateResponse(SlackMessageResponder responder, SlackMessagePosted postedMessage) {
        HashMap<String, String> contextMap =responder.getSlackService().getContextMap();
        GitHub github = responder.getGitHub();

        String repository = contextMap.get("repository");
        String commitSha = contextMap.get("commitSha");

        if (repository == null || commitSha == null) {
            return new SlackErrorMessage("I haven't mentioned any changes.");
        }

        try {
            GHRepository ghRepo = github.getRepository(repository);
            GHCommit ghCommit = ghRepo.getCommit(commitSha);

            return new CommitInfoMessage(ghCommit);

        } catch (IOException e) {
            return new SlackErrorMessage();
        }

    }
}
