package com.iba.security.oauth2.oauth2_user;

import java.util.Map;

public abstract class OAuth2UserInfo {
    public Map<String, Object> attributes;

    OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getFirstName();

    public abstract String getLastName();

    public abstract String getName();

    public abstract String getEmail();

    public abstract boolean getEmailVerified();

    public abstract String getImageUrl();
}
