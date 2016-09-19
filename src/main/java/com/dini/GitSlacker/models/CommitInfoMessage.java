package com.dini.GitSlacker.models;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.kohsuke.github.GHCommit;

import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */
public class CommitInfoMessage implements SlackMessageTemplate {

    GHCommit commit;

    public CommitInfoMessage(GHCommit commit) {
        this.commit = commit;
    }

    @Override
    public HashMap<String, String> getLatestContextVariables() {
        return null;
    }

    @Override
    public SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments) {
        StringBuilder message = new StringBuilder();

        message.append("here's what changed");

        return new SlackPreparedMessage.Builder()
                .withMessage(message.toString())
                .build();
    }

    @Override
    public SlackPreparedMessage generateMessage() {
        return generateMessage(null);
    }
}
