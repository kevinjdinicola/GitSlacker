package com.dini.GitSlacker.util;

import org.kohsuke.github.GHEvent;

import java.util.HashMap;

/**
 * Created by kevin on 9/15/16.
 */
public enum GitHubMessageTemplate {

    IssueCommented( GHEvent.ISSUE_COMMENT, "%s commented on issue '%s' in %s",           new String[]{"actor", "issue.title", "repository"}),
    IssueCreated(   GHEvent.ISSUES,        "%s %s issue '%s' in %s",                 new String[]{"actor", "action", "issue.title", "repository"}),
    Push(           GHEvent.PUSH,          "%s pushed a commit to '%s' in %s (sha: %s)", new String[]{"actor","ref","repository", "head"}),
    CreateBranch(   GHEvent.CREATE,        "%s created new %s '%s' in %s",               new String[]{"actor","ref_type","ref","repository"}),
    PullRequest(    GHEvent.PULL_REQUEST,  "%s %s pull request called '%s'.",          new String[]{"actor","action","pull_request.title"});

    private GHEvent eventType;
    private String template;
    private String[] payloadArgumentPaths;

    private static HashMap<String, GitHubMessageTemplate> map = new HashMap<>();

    GitHubMessageTemplate(GHEvent eventType, String template, String[] payloadArgumentPaths) {
        this.eventType = eventType;
        this.template = template;
        this.payloadArgumentPaths = payloadArgumentPaths;
    }

    public String getTemplate() {
        return template;
    }

    public String[] getArguments() {
        return payloadArgumentPaths;
    }

    public static GitHubMessageTemplate getByKind(GHEvent eventType) {
        GitHubMessageTemplate[] v = GitHubMessageTemplate.values();
        for (int i = 0; i < v.length; i++ ) {
            if (v[i].eventType.equals(eventType)) {
                return v[i];
            }
        }
        return null;
    }

}
