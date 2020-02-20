package com.iba.security.oauth2.oauth2_user;

import java.util.Map;

public class GitlabOAuth2UserInfo extends OAuth2UserInfo {

    GitlabOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public String getName() {
        return (String) attributes.get("username");
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
