package com.iba.security.oauth2.jwt;

import com.iba.config.OAuth2Config;
import com.iba.model.user.User;
import com.iba.repository.UserRepository;
import com.iba.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private OAuth2Config oAuth2Config;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    public JwtAuthenticationTokenFilter() {
        super("/projects/**");
        setRequiresAuthenticationRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/projects/**"),
                new AntPathRequestMatcher("/lang/**"),
                new AntPathRequestMatcher("/project-lang/**"),
                new AntPathRequestMatcher("/contributors/**"),
                new AntPathRequestMatcher("/terms/**"),
                new AntPathRequestMatcher("/term-lang/**"),
                new AntPathRequestMatcher("/user/**"),
                new AntPathRequestMatcher("/search/**"),
                new AntPathRequestMatcher("/statistic/**"),
                new AntPathRequestMatcher("/history/**"),
                new AntPathRequestMatcher("/image/**"),
                new AntPathRequestMatcher("/glossary/**"),
                new AntPathRequestMatcher("/integration/**")
        ));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
                                                HttpServletResponse httpServletResponse)
            throws AuthenticationException, IOException {

        long now = new Date().getTime();
        String accessHeader = httpServletRequest.getHeader("Auth");
        String refreshHeader = httpServletRequest.getHeader("AuthRef");
        if (accessHeader == null || !accessHeader.startsWith("Token ") || refreshHeader == null || !refreshHeader.startsWith("Refresh ")) {
            httpServletResponse.sendError(420, "UNAUTHORIZED");
            // throw new RuntimeException("JWT Token is missing");
            return null;
        }
        String accessToken = accessHeader.substring(6);
        String refreshToken = refreshHeader.substring(8);

        JwtUser accessUser = tokenProvider.validateAccess(accessToken);
        JwtUser refreshUser = tokenProvider.validateRefresh(refreshToken);

        if (accessUser == null || refreshUser == null) {
            httpServletResponse.sendError(420, "UNAUTHORIZED");
            return null;
        }

        User existUser = userRepository.findById(refreshUser.getId());
        if (!existUser.getRefreshToken().equals(refreshToken)) {
            httpServletResponse.sendError(420, "UNAUTHORIZED");
            return null;
        }
        if ((now - refreshUser.getDate().getTime()) > oAuth2Config.getJwt().getRefresh().getTokenExpirationMsecs()) {
            String newRefreshToken = tokenProvider.generateRefresh(refreshUser);
            existUser.setRefreshToken(newRefreshToken);
            userRepository.save(existUser);
            httpServletResponse.sendError(420, "UNAUTHORIZED");
            return null;
        }
        if ((now - accessUser.getDate().getTime()) > oAuth2Config.getJwt().getAccess().getTokenExpirationMsecs()) {
            String newRefreshToken = tokenProvider.generateRefresh(refreshUser);
            existUser.setRefreshToken(newRefreshToken);
            userRepository.save(existUser);
            httpServletResponse.sendError(401, "UNAUTHORIZED");
            return null;
        }
        JwtAuthenticationToken token = new JwtAuthenticationToken(accessToken);
        SecurityContextHolder.getContext()
                .setAuthentication(token);
        return getAuthenticationManager().authenticate(token);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
