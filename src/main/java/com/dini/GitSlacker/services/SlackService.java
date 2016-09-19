package com.dini.GitSlacker.services;

import com.dini.GitSlacker.messages.SlackMessageTemplate;
import com.dini.GitSlacker.responders.SlackMessageResponseGenerator;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import io.dropwizard.lifecycle.Managed;

import java.util.HashMap;

/**
 * Created by kevin on 9/13/16.
 */
public interface SlackService extends Managed {

    void createNotifier(String gitRepo, String channel);
    void addResponder(SlackMessageResponseGenerator responseGenerator);
    void sendMessage(SlackMessageTemplate slackMsg, String channel, HashMap<String, String> overrideArguments);
    void sendMessage(SlackMessageTemplate slackMsg, String channel);
    HashMap<String, String> getContextMap();
}
