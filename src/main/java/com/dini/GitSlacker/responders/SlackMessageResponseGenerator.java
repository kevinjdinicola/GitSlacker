package com.dini.GitSlacker.responders;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

/**
 * Created by kevin on 9/18/16.
 */
public interface SlackMessageResponseGenerator {

    boolean shouldRespond(SlackMessagePosted postedMessage);
    SlackMessageTemplate generateResponse(SlackMessageResponder responder, SlackMessagePosted postedMessage);
}
