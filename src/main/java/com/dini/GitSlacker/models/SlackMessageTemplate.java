package com.dini.GitSlacker.models;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;

import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */
public interface SlackMessageTemplate {

    HashMap<String, String> getLatestContextVariables();
    SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments);
    SlackPreparedMessage generateMessage();
}
