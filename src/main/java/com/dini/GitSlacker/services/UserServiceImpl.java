package com.dini.GitSlacker.services;

import com.dini.GitSlacker.models.User;
import com.dini.GitSlacker.config.GitSlackerConfiguration;
import com.google.inject.Inject;

import java.util.HashMap;

/**
 * Created by kevin on 9/17/16.
 */
public class UserServiceImpl implements UserService {

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
        return gitHubLoginMap.get(githubLogin);
    }
}
