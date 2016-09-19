package com.dini.GitSlacker.messages;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kevin on 9/18/16.
 */
public class CommitInfoMessage implements SlackMessageTemplate {

    GHCommit commit;
    List<GHCommit.File> changedFiles;

    public CommitInfoMessage(GHCommit commit) throws IOException {
        this.commit = commit;
        this.changedFiles = commit.getFiles();
    }

    @Override
    public HashMap<String, String> getLatestContextVariables() {
        return new HashMap<>();
    }

    @Override
    public SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments) {
        StringBuilder message = new StringBuilder();

        message.append("Here's what's changed: \n");

        changedFiles.forEach(file -> {
            message.append("[ " + file.getStatus() + " ] " + file.getFileName() + "\n");
        });
        message.append("Say \"show me example/file/name.java\" to see the file in question");

        return new SlackPreparedMessage.Builder()
                .withMessage(message.toString())
                .build();
    }

    @Override
    public SlackPreparedMessage generateMessage() {
        return generateMessage(null);
    }
}
