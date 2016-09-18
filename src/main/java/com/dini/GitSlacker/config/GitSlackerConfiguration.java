package com.dini.GitSlacker.config;

import com.dini.GitSlacker.models.User;
import com.dini.GitSlacker.models.WatchedRepository;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by kevin on 9/13/16.
 */
@Getter
@Setter
public class GitSlackerConfiguration extends Configuration {
    List<User> userMap;
    List<WatchedRepository> watchedRepositories;
    String slackApiKey;
    String githubUsername;
    String githubPassword;
}
