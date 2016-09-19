package com.dini.GitSlacker.messages;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;

import java.util.HashMap;

/**
 * Created by kevin on 9/18/16.
 */

/*
  Interfaces should be able to generate a slack message and provide
  any context variables to the slack service that should be remembered
  for future responses.  Some messages may take arguments that can be overridden
 */
public interface SlackMessageTemplate {

    HashMap<String, String> getLatestContextVariables();
    SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments);
    SlackPreparedMessage generateMessage();
}
