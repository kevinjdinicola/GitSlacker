package com.dini.GitSlacker.messages;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;

import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */
public class SlackErrorMessage implements SlackMessageTemplate {

    String overrideMessage = null;

    public SlackErrorMessage() {}

    public SlackErrorMessage(String overrideMessage) {
        this.overrideMessage = overrideMessage;
    }

    @Override
    public HashMap<String, String> getLatestContextVariables() {
        return new HashMap<>();
    }

    @Override
    public SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments) {
        return generateMessage();
    }

    @Override
    public SlackPreparedMessage generateMessage() {
        return new SlackPreparedMessage.Builder()
                .withMessage(overrideMessage != null ? overrideMessage : "I'm sorry, but and unexpected error occurred and I could not process your request.")
                .build();
    }
}
