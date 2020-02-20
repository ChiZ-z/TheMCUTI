package com.iba.security.oauth2.oauth2_user;

import com.iba.exceptions.Exception_403;
import com.iba.model.view.Constants;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        Constants.AuthProvider provider = Enum.valueOf(Constants.AuthProvider.class, registrationId.toLowerCase());
        switch (provider) {
            case google: {
                return new GoogleOAuth2UserInfo(attributes);
            }
            case facebook: {
                return new FacebookOAuth2UserInfo(attributes);
            }
            case github:
            case github_repo: {
                return new GithubOAuth2UserInfo(attributes);
            }
            case gitlab: {
                return new GitlabOAuth2UserInfo(attributes);
            }
            default: {
                throw new Exception_403("Sorry! Login with " + registrationId + " is not supported yet.");
            }
        }
    }
}
