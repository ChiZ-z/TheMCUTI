package com.iba.config;

import com.iba.model.view.Constants;
import com.iba.security.oauth2.CustomOAuth2UserService;
import com.iba.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.iba.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.iba.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.iba.security.oauth2.jwt.JwtAuthenticationTokenFilter;
import com.iba.security.oauth2.jwt.JwtSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2ClientConfig oAuth2ClientConfig;

    @Autowired
    public WebSecurityConfig(OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                             OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                             CustomOAuth2UserService customOAuth2UserService, OAuth2ClientConfig oAuth2ClientConfig) {
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2ClientConfig = oAuth2ClientConfig;
    }

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilter() throws Exception {
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(new JwtSuccessHandler());
        return filter;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = Arrays.stream(Constants.AuthProvider.values())
                .map(this::getRegistration)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(Constants.AuthProvider client) {
        switch (client) {
            case google: {
                return CommonOAuth2Provider.GOOGLE.getBuilder(client.toString())
                        .clientId(oAuth2ClientConfig.getRegistration().getGoogle().getClientId())
                        .clientSecret(oAuth2ClientConfig.getRegistration().getGoogle().getClientSecret())
                        .redirectUriTemplate(oAuth2ClientConfig.getRegistration().getGoogle().getRedirectUriTemplate())
                        .scope(oAuth2ClientConfig.getRegistration().getGoogle().getScope()).build();
            }
            case facebook: {
                return CommonOAuth2Provider.FACEBOOK.getBuilder(client.toString())
                        .clientId(oAuth2ClientConfig.getRegistration().getFacebook().getClientId())
                        .clientSecret(oAuth2ClientConfig.getRegistration().getFacebook().getClientSecret())
                        .redirectUriTemplate(oAuth2ClientConfig.getRegistration().getFacebook().getRedirectUriTemplate())
                        .scope(oAuth2ClientConfig.getRegistration().getFacebook().getScope()).build();
            }
            case github: {
                return CommonOAuth2Provider.GITHUB.getBuilder(client.toString())
                        .clientId(oAuth2ClientConfig.getRegistration().getGithub().getClientId())
                        .clientSecret(oAuth2ClientConfig.getRegistration().getGithub().getClientSecret())
                        .redirectUriTemplate(oAuth2ClientConfig.getRegistration().getGithub().getRedirectUriTemplate())
                        .scope(oAuth2ClientConfig.getRegistration().getGithub().getScope()).build();
            }
            case github_repo: {
                return CommonOAuth2Provider.GITHUB.getBuilder(client.toString())
                        .clientId(oAuth2ClientConfig.getRegistration().getGithubRepo().getClientId())
                        .clientSecret(oAuth2ClientConfig.getRegistration().getGithubRepo().getClientSecret())
                        .redirectUriTemplate(oAuth2ClientConfig.getRegistration().getGithubRepo().getRedirectUriTemplate())
                        .scope(oAuth2ClientConfig.getRegistration().getGithubRepo().getScope()).build();
            }
            case gitlab: {
                return ClientRegistration.withRegistrationId(client.toString())
                        .clientId(oAuth2ClientConfig.getRegistration().getGitlab().getClientId())
                        .clientSecret(oAuth2ClientConfig.getRegistration().getGitlab().getClientSecret())
                        .redirectUriTemplate(oAuth2ClientConfig.getRegistration().getGitlab().getRedirectUriTemplate())
                        .authorizationGrantType(oAuth2ClientConfig.getRegistration().getGitlab().getAuthorizationGrantType())
                        .scope(oAuth2ClientConfig.getRegistration().getGitlab().getScope())
                        .authorizationUri(oAuth2ClientConfig.getProvider().getGitlab().getAuthorizationUri())
                        .tokenUri(oAuth2ClientConfig.getProvider().getGitlab().getTokenUri())
                        .userInfoUri(oAuth2ClientConfig.getProvider().getGitlab().getUserInfoUri())
                        .jwkSetUri(oAuth2ClientConfig.getProvider().getGitlab().getJwkSetUri())
                        .userNameAttributeName(oAuth2ClientConfig.getProvider().getGitlab().getUserNameAttribute()).build();
            }
            case bitbucket: {
                return null;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.headers().cacheControl();
        http.cors().and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .authorizeRequests()
                .antMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js")
                .permitAll()
                .antMatchers("/auth/**", "/util/**", "/logout/**").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/authorize")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .and()
                .addFilterAfter(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
