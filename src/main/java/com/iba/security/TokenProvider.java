package com.iba.security;

import com.iba.config.OAuth2Config;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.view.Constants;
import com.iba.security.oauth2.jwt.JwtUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenProvider {

    private final OAuth2Config oAuth2Config;

    @Autowired
    public TokenProvider(OAuth2Config oAuth2Config) {
        this.oAuth2Config = oAuth2Config;
    }

    public String generateOAuth2(JwtUser jwtUser, String refreshToken, Constants.AuthProvider provider) {
        Claims claims = Jwts.claims().setSubject("OAuth2");
        claims.put("Token", generateAccess(jwtUser));
        claims.put("Refresh", refreshToken);
        claims.put("Provider", provider.toString());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, oAuth2Config.getSecret()).compact();
    }

    public String generateIntegrationAccess(OAuth2AccessToken oAuth2AccessToken, Constants.AuthProvider provider) {
        switch (provider) {
            case github: {
                Claims claims = Jwts.claims().setSubject("AccessGithub");
                claims.put("Token", oAuth2AccessToken.getTokenValue());
                claims.put("Provider", provider.toString());
                return Jwts.builder()
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS512, oAuth2Config.getSecret()).compact();
            }
            case gitlab: {
                Claims claims = Jwts.claims().setSubject("AccessGitlab");
                claims.put("Token", oAuth2AccessToken.getTokenValue());
                claims.put("Provider", provider.toString());
                return Jwts.builder()
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS512, oAuth2Config.getSecret()).compact();
            }
            case bitbucket: {
                Claims claims = Jwts.claims().setSubject("AccessBitbucket");
                claims.put("Token", oAuth2AccessToken.getTokenValue());
                claims.put("Provider", provider.toString());
                return Jwts.builder()
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS512, oAuth2Config.getSecret()).compact();
            }
            default: {
                throw new Exception_404("Provider not found");
            }
        }
    }

    public String generateAccess(JwtUser jwtUser) {
        return generate(jwtUser, oAuth2Config.getJwt().getAccess().getSecret());
    }

    public String generateRefresh(JwtUser jwtUser) {
        return generate(jwtUser, oAuth2Config.getJwt().getRefresh().getSecret());
    }

    public JwtUser validateAccess(String token) {
        return validate(token, oAuth2Config.getJwt().getAccess().getSecret());
    }

    public JwtUser validateRefresh(String token) {
        return validate(token, oAuth2Config.getJwt().getRefresh().getSecret());
    }

    private String generate(JwtUser jwtUser, String secret) {
        Claims claims = Jwts.claims().setSubject(jwtUser.getUserName());
        claims.put("userId", String.valueOf(jwtUser.getId()));
        claims.put("date", new Date().getTime());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }


    private JwtUser validate(String token, String secret) {
        JwtUser jwtUser = null;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            jwtUser = new JwtUser(Long.parseLong((String) body.get("userId")), body.getSubject(), (long) body.get("date"));
        } catch (Exception e) {
            System.out.printf("Error jwt = %s%n", e);
        }
        return jwtUser;
    }
}
