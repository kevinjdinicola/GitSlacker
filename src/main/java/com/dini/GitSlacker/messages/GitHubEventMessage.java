package com.dini.GitSlacker.messages;

import com.dini.GitSlacker.models.SlackMessageTemplate;
import com.dini.GitSlacker.util.GitHubMessageTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by kevin on 9/14/16.
 */

public class GitHubEventMessage implements SlackMessageTemplate {

    Logger log = LoggerFactory.getLogger(GitHubEventMessage.class);

    @Getter
    @Setter
    String repositoryName;
    @Getter
    @Setter
    GHEventInfo event;

    public GitHubEventMessage(String repositoryName, GHEventInfo event) {
        this.repositoryName = repositoryName;
        this.event = event;
    }

    private ObjectNode payloadCache;

    private String[] mapTemplateArguments(ObjectNode payload, String[] argChoices) {
        String[] mappedArguments = new String[argChoices.length];

        for (int i = 0; i < argChoices.length; i++) {
            mappedArguments[i] = recursiveFindValue(payload, argChoices[i]);
        }

        return mappedArguments;
    }

    private String recursiveFindValue(JsonNode node, String valuePath) {
        int dotIndex = valuePath.indexOf(".");
        if (dotIndex < 0) {
            return node.findValue(valuePath).asText();
        } else {
            JsonNode nextNode = node.findValue(valuePath.substring(0,dotIndex));
            if (nextNode != null) {
                return recursiveFindValue(nextNode, valuePath.substring(dotIndex+1));
            } else {
                return "(null)";
            }
        }
    }

    public HashMap<String, String> getLatestContextVariables() {
        HashMap<String, String> newContexts = new HashMap<>();
        GHEvent type = getEvent().getType();

        ObjectNode payload = null;
        try {
            payload = getPayload();
        } catch (NoSuchFieldException e) {
            log.error("Error extracting latest context variables from message");
            log.error(e.toString());
            return newContexts;
        } catch (IllegalAccessException e) {
            log.error("Error extracting latest context variables from message");
            log.error(e.toString());
            return newContexts;
        }

        if (type.equals(GHEvent.PUSH)) {
            newContexts.put("repository",repositoryName);
            newContexts.put("commitSha", recursiveFindValue(payload,"head"));
        }

        return newContexts;
    }

    public SlackPreparedMessage generateMessage() {
        return generateMessage(null);
    }

    private ObjectNode getPayload() throws NoSuchFieldException, IllegalAccessException {
        if (payloadCache == null) {
            Field f = event.getClass().getDeclaredField("payload");
            f.setAccessible(true);
            payloadCache = (ObjectNode)f.get(event);
        }
        return payloadCache;
    }

    public SlackPreparedMessage generateMessage(HashMap<String, String> overrideArguments) {
        String message = "";
        GHEventInfo event = getEvent();

        try {
            GitHubMessageTemplate template = GitHubMessageTemplate.getByKind(event.getType());

            if (template == null) {
                return null;
            }

            ObjectNode payload = getPayload();

            // preload some convenient values not part of the event payload that are
            // useful in the template
            payload.put("actor", event.getActorLogin());
            payload.put("repository", repositoryName);

            if (overrideArguments != null) {
                for (String key : overrideArguments.keySet()) {
                    payload.put(key,overrideArguments.get(key));
                }
            }

            message = String.format(template.getTemplate(), mapTemplateArguments(payload, template.getArguments()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        SlackPreparedMessage.Builder mBuilder = new SlackPreparedMessage.Builder();
        mBuilder.withLinkNames(true);
        mBuilder.withMessage(message);

        return mBuilder.build();
    }
}
