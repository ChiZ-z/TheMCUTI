package com.iba.security.oauth2.oauth2_user;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public String getName() {
        return (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public boolean getEmailVerified() {
        return true;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
