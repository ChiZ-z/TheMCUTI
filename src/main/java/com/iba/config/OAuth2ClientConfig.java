package com.iba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuth2ClientConfig {
    private Registration registration = new Registration();
    private Provider provider = new Provider();

    public static class Registration {
        private final Google google = new Google();
        private final Facebook facebook = new Facebook();
        private final Github github = new Github();
        private final GithubRepo githubRepo = new GithubRepo();
        private final Gitlab gitlab = new Gitlab();

        public static class Google {
            private String clientId;
            private String clientSecret;
            private String redirectUriTemplate;
            private List<String> scope = new ArrayList<>();

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getRedirectUriTemplate() {
                return redirectUriTemplate;
            }

            public void setRedirectUriTemplate(String redirectUriTemplate) {
                this.redirectUriTemplate = redirectUriTemplate;
            }

            public List<String> getScope() {
                return scope;
            }

            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }

        public static class Facebook {
            private String clientId;
            private String clientSecret;
            private String redirectUriTemplate;
            private List<String> scope = new ArrayList<>();

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getRedirectUriTemplate() {
                return redirectUriTemplate;
            }

            public void setRedirectUriTemplate(String redirectUriTemplate) {
                this.redirectUriTemplate = redirectUriTemplate;
            }

            public List<String> getScope() {
                return scope;
            }

            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }

        public static class Github {
            private String clientId;
            private String clientSecret;
            private String redirectUriTemplate;
            private List<String> scope = new ArrayList<>();

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getRedirectUriTemplate() {
                return redirectUriTemplate;
            }

            public void setRedirectUriTemplate(String redirectUriTemplate) {
                this.redirectUriTemplate = redirectUriTemplate;
            }

            public List<String> getScope() {
                return scope;
            }

            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }

        public static class GithubRepo {
            private String clientId;
            private String clientSecret;
            private String redirectUriTemplate;
            private List<String> scope = new ArrayList<>();

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getRedirectUriTemplate() {
                return redirectUriTemplate;
            }

            public void setRedirectUriTemplate(String redirectUriTemplate) {
                this.redirectUriTemplate = redirectUriTemplate;
            }

            public List<String> getScope() {
                return scope;
            }

            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }

        public static class Gitlab {
            private String clientId;
            private String clientSecret;
            private String redirectUriTemplate;
            private AuthorizationGrantType authorizationGrantType;
            private List<String> scope = new ArrayList<>();

            String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getRedirectUriTemplate() {
                return redirectUriTemplate;
            }

            public void setRedirectUriTemplate(String redirectUriTemplate) {
                this.redirectUriTemplate = redirectUriTemplate;
            }

            public AuthorizationGrantType getAuthorizationGrantType() {
                return authorizationGrantType;
            }

            public void setAuthorizationGrantType(AuthorizationGrantType authorizationGrantType) {
                this.authorizationGrantType = authorizationGrantType;
            }

            public List<String> getScope() {
                return scope;
            }

            public void setScope(List<String> scope) {
                this.scope = scope;
            }
        }

        public Google getGoogle() {
            return google;
        }

        public Facebook getFacebook() {
            return facebook;
        }

        public Github getGithub() {
            return github;
        }

        public GithubRepo getGithubRepo() {
            return githubRepo;
        }

        public Gitlab getGitlab() {
            return gitlab;
        }
    }

    public static class Provider {
        private final Gitlab gitlab = new Gitlab();

        public static class Gitlab {
            private String authorizationUri;
            private String tokenUri;
            private String userInfoUri;
            private String jwkSetUri;
            private String userNameAttribute;

            public String getAuthorizationUri() {
                return authorizationUri;
            }

            public void setAuthorizationUri(String authorizationUri) {
                this.authorizationUri = authorizationUri;
            }

            public String getTokenUri() {
                return tokenUri;
            }

            public void setTokenUri(String tokenUri) {
                this.tokenUri = tokenUri;
            }

            public String getUserInfoUri() {
                return userInfoUri;
            }

            public void setUserInfoUri(String userInfoUri) {
                this.userInfoUri = userInfoUri;
            }

            public String getJwkSetUri() {
                return jwkSetUri;
            }

            public void setJwkSetUri(String jwkSetUri) {
                this.jwkSetUri = jwkSetUri;
            }

            public String getUserNameAttribute() {
                return userNameAttribute;
            }

            public void setUserNameAttribute(String userNameAttribute) {
                this.userNameAttribute = userNameAttribute;
            }
        }

        public Gitlab getGitlab() {
            return gitlab;
        }
    }

    public Registration getRegistration() {
        return registration;
    }

    public Provider getProvider() {
        return provider;
    }
}
