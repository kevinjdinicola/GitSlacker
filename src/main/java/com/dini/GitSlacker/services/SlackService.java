package com.dini.GitSlacker.services;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import io.dropwizard.lifecycle.Managed;

/**
 * Created by kevin on 9/13/16.
 */
public interface SlackService extends Managed {

    void createNotifier(String gitRepo, String channel);
    void sendMessage(SlackPreparedMessage slackMsg, String channel);

}
