package com.dini.GitSlacker.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by kevin on 9/17/16.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String githubLogin;
    private String slackName;
}
