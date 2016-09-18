package com.dini.GitSlacker.services;

import com.dini.GitSlacker.models.User;

/**
 * Created by kevin on 9/17/16.
 */
public interface UserService {

    User getUserByGitHubLogin(String githubLogin);

}
