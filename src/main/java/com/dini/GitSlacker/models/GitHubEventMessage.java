package com.dini.GitSlacker.models;

import com.dini.GitSlacker.util.GitHubMessageTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.kohsuke.github.GHEventInfo;

import java.lang.reflect.Field;

/**
 * Created by kevin on 9/14/16.
 */
@Getter
@Setter
@AllArgsConstructor
public class GitHubEventMessage {

    String repositoryName;
    GHEventInfo event;

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

    public SlackPreparedMessage generateMessage(String slackActor) {
        String message = "";
        GHEventInfo event = getEvent();

        try {
            GitHubMessageTemplate template = GitHubMessageTemplate.getByKind(event.getType());

            if (template == null) {
                return null;
            }

            Field f = event.getClass().getDeclaredField("payload");
            f.setAccessible(true);
            ObjectNode payload = (ObjectNode)f.get(event);

            // preload some convenient values not part of the event payload that are
            // useful in the template
            payload.put("actor", slackActor != null ? "@"+slackActor : event.getActorLogin());
            payload.put("repository", repositoryName);

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
