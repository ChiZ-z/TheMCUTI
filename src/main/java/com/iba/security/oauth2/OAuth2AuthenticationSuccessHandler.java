package com.iba.security.oauth2;


import com.iba.config.OAuth2Config;
import com.iba.exceptions.Exception_400;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.UserRepository;
import com.iba.security.TokenProvider;
import com.iba.security.oauth2.jwt.JwtUser;
import com.iba.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Optional;

import static com.iba.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private TokenProvider tokenProvider;

    private OAuth2Config OAuth2Config;

    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private UserRepository userRepository;

    @Autowired
    OAuth2AuthenticationSuccessHandler(TokenProvider tokenProvider, OAuth2Config OAuth2Config,
                                       HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.OAuth2Config = OAuth2Config;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }
        clearAuthenticationAttributes(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        if (redirectUri.isPresent() && isAuthorizedRedirectUri(redirectUri.get())) {
            String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            User user = (User) authentication.getPrincipal();
            if (targetUrl.equalsIgnoreCase(OAuth2Config.getAuthorizedRedirectUris().get(0))) {
                JwtUser jwtUser = new JwtUser(user.getId(),
                        user.getName(), new Date().getTime());
                parameters.add("OAuth2", tokenProvider.generateOAuth2(jwtUser, user.getRefreshToken(), user.getProvider()));
            } else if (targetUrl.equalsIgnoreCase(OAuth2Config.getAuthorizedRedirectUris().get(1))) {
                parameters.add("AccessGithub", tokenProvider.generateIntegrationAccess(
                        CustomOAuth2UserService.oAuth2AccessToken, Constants.AuthProvider.github));
            } else if (targetUrl.equalsIgnoreCase(OAuth2Config.getAuthorizedRedirectUris().get(2))) {
                parameters.add("AccessGitlab", tokenProvider.generateIntegrationAccess(
                        CustomOAuth2UserService.oAuth2AccessToken, Constants.AuthProvider.gitlab));
            } else if (targetUrl.equalsIgnoreCase(OAuth2Config.getAuthorizedRedirectUris().get(3))) {
                parameters.add("AccessBitbucket", tokenProvider.generateIntegrationAccess(
                        CustomOAuth2UserService.oAuth2AccessToken, Constants.AuthProvider.bitbucket));
            }
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParams(parameters)
                    .build().toUriString();
        } else {
            throw new Exception_400("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return OAuth2Config.getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
