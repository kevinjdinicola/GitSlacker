package com.dini.GitSlacker.services;

import com.dini.GitSlacker.models.User;
import com.dini.GitSlacker.config.GitSlackerConfiguration;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by kevin on 9/17/16.
 */
public class UserServiceImpl implements UserService {

    Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    GitSlackerConfiguration config;

    HashMap<String, User> gitHubLoginMap;

    @Inject
    public UserServiceImpl(GitSlackerConfiguration config) {
        this.config = config;
        gitHubLoginMap = new HashMap<>();
        config.getUserMap().forEach(user -> gitHubLoginMap.put(user.getGithubLogin(),user));
    }

    @Override
    public User getUserByGitHubLogin(String githubLogin) {
        if (gitHubLoginMap.containsKey(githubLogin)) {
            return gitHubLoginMap.get(githubLogin);
        } else {
            log.warn("User service cannot determine slack name for github user: " + githubLogin);
            return null;
        }
    }
}
