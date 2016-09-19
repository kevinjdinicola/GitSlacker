package com.dini.GitSlacker.messages;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.kohsuke.github.GHCommit;

import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */
public class CommitFileInfoMessage  implements SlackMessageTemplate {

    GHCommit.File file;

    public CommitFileInfoMessage(GHCommit.File file) {
        this.file = file;
    }

    @Override
    public HashMap<String, String> getLatestContextVariables() {
        return null;
    }

    @Override
    public SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments) {
        return generateMessage();
    }

    @Override
    public SlackPreparedMessage generateMessage() {
        return new SlackPreparedMessage.Builder()
                .withMessage("You can grab a copy from GitHub: " + file.getRawUrl().toString())
                .build();
    }
}
