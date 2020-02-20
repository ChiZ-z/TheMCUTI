package com.iba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Config {
    private String secret;
    private List<String> authorizedRedirectUris = new ArrayList<>();

    private JWT jwt = new JWT();

    public static class JWT {
        private final Access access = new Access();
        private final Refresh refresh = new Refresh();

        public static class Access {
            private String name;
            private String secret;
            private Long tokenExpirationMsecs;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public Long getTokenExpirationMsecs() {
                return tokenExpirationMsecs;
            }

            public void setTokenExpirationMsecs(Long tokenExpirationMsecs) {
                this.tokenExpirationMsecs = tokenExpirationMsecs;
            }
        }

        public static class Refresh {
            private String name;
            private String secret;
            private Long tokenExpirationMsecs;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public Long getTokenExpirationMsecs() {
                return tokenExpirationMsecs;
            }

            public void setTokenExpirationMsecs(Long tokenExpirationMsecs) {
                this.tokenExpirationMsecs = tokenExpirationMsecs;
            }
        }

        public Access getAccess() {
            return access;
        }

        public Refresh getRefresh() {
            return refresh;
        }

    }

    public JWT getJwt() {
        return jwt;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<String> getAuthorizedRedirectUris() {
        return authorizedRedirectUris;
    }

    public void setAuthorizedRedirectUris(List<String> authorizedRedirectUris) {
        this.authorizedRedirectUris = authorizedRedirectUris;
    }
}
