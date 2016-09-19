package com.dini.GitSlacker.responders;

import com.dini.GitSlacker.messages.CommitFileInfoMessage;
import com.dini.GitSlacker.messages.SlackErrorMessage;
import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by kevin on 9/18/16.
 */
public class CommitFileInfoResponder implements SlackMessageResponseGenerator {

    private final static String COMMAND = "show me ";

    @Override
    public boolean shouldRespond(SlackMessagePosted postedMessage) {

        // simple message parsing, im not going to create natural language processing here...

        String content = postedMessage.getMessageContent().toLowerCase();
        if (content.startsWith(COMMAND) && content.length() > COMMAND.length()) {
            //starts with show me and has a file name afterwords
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
        String filename = postedMessage.getMessageContent().substring(COMMAND.length()); // 8 is e

        if (repository == null || commitSha == null) {
            return new SlackErrorMessage();
        }

        try {
            GHRepository ghRepo = github.getRepository(repository);
            GHCommit ghCommit = ghRepo.getCommit(commitSha);

            Optional<GHCommit.File> requestedFile = ghCommit.getFiles().stream()
                    .filter(file -> file.getFileName().equals(filename)).findFirst();

            if (requestedFile.isPresent()) {
                return new CommitFileInfoMessage(requestedFile.get());
            } else {
                return new SlackErrorMessage("The specified file was not found");
            }

        } catch (IOException e) {
            return new SlackErrorMessage();
        }
    }
}
