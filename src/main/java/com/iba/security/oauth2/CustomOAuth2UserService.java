
package com.iba.security.oauth2;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.UserRepository;
import com.iba.security.TokenProvider;
import com.iba.security.oauth2.jwt.JwtUser;
import com.iba.security.oauth2.oauth2_user.GitHubEmail;
import com.iba.security.oauth2.oauth2_user.OAuth2UserInfo;
import com.iba.security.oauth2.oauth2_user.OAuth2UserInfoFactory;
import com.iba.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final String USER_AGENT = "Mozilla/5.0";

    static OAuth2AccessToken oAuth2AccessToken;

    private final UserRepository userRepository;

    private final FileUtils fileUtils;

    private final TokenProvider tokenProvider;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository, FileUtils fileUtils, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.fileUtils = fileUtils;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) throws IOException, JSONException {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        Constants.AuthProvider provider = Enum.valueOf(Constants.AuthProvider.class, oAuth2UserRequest.getClientRegistration().getRegistrationId().toLowerCase());
        switch (provider) {
            case google:
            case facebook:
                return socialLogin(oAuth2UserInfo, oAuth2UserRequest, oAuth2User);
            case github: {
                return loginByGitHub(oAuth2UserInfo, oAuth2UserRequest, oAuth2User);
            }
            case github_repo:
            case gitlab: {
                oAuth2AccessToken = oAuth2UserRequest.getAccessToken();
                return new User(oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail(), oAuth2UserInfo.getFirstName(), oAuth2UserInfo.getLastName(),
                        oAuth2UserInfo.getImageUrl() != null ? fileUtils.getImageFromURL(new URL(oAuth2UserInfo.getImageUrl())) : null,
                        provider, oAuth2UserInfo.getId(), oAuth2UserInfo.getEmailVerified(), oAuth2User.getAttributes());
            }
            default: {
                throw new Exception_403("Sorry! Login with " + oAuth2UserRequest.getClientRegistration().getRegistrationId() + " is not supported yet.");
            }
        }
    }

    private OAuth2User socialLogin(OAuth2UserInfo oAuth2UserInfo, OAuth2UserRequest oAuth2UserRequest, OAuth2User
            oAuth2User) throws IOException {
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new Exception_404("Email not found from OAuth2 provider");
        }
        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        if (user != null) {
            if (!user.getProvider().equals(Constants.AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new Exception_403("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo.getFirstName(), oAuth2UserInfo.getLastName(),
                    oAuth2UserInfo.getImageUrl() != null ? fileUtils.getImageFromURL(new URL(oAuth2UserInfo.getImageUrl())) : null);
        } else {
            user = registerNewUser(oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail(), oAuth2UserInfo.getFirstName(), oAuth2UserInfo.getLastName(),
                    oAuth2UserInfo.getImageUrl() != null ? fileUtils.getImageFromURL(new URL(oAuth2UserInfo.getImageUrl())) : null,
                    Constants.AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()),
                    oAuth2UserInfo.getId(), oAuth2UserInfo.getEmailVerified(), oAuth2User.getAttributes());
        }
        return user;
    }

    private OAuth2User loginByGitHub(OAuth2UserInfo oAuth2UserInfo, OAuth2UserRequest oAuth2UserRequest, OAuth2User
            oAuth2User) throws IOException {
        String email = getEmailFromGitHub(oAuth2UserRequest);
        oAuth2AccessToken = oAuth2UserRequest.getAccessToken();
        if (email == null) {
            throw new Exception_404("Email not found from OAuth2 provider");
        }
        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (!user.getProvider().equals(Constants.AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new Exception_403("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, null, null, null);
        } else {
            user = registerNewUser(oAuth2UserInfo.getName(), email, oAuth2UserInfo.getFirstName(), oAuth2UserInfo.getLastName(),
                    null, Constants.AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()),
                    oAuth2UserInfo.getId(), oAuth2UserInfo.getEmailVerified(), oAuth2User.getAttributes());
        }

        return user;
    }

    private User registerNewUser(String username, String email, String firstName, String lastName,
                                 String image, Constants.AuthProvider provider,
                                 String providerId, boolean emailVerified, Map<String, Object> attributes) {
        User user = new User(username, email, firstName, lastName,
                image, provider, providerId, emailVerified, attributes);
        userRepository.save(user);
        user.setRefreshToken(tokenProvider.generateRefresh(new JwtUser(user.getId(), user.getEmail(), new Date().getTime())));
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, String firstName, String lastName, String image) {
        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setProfilePhoto(image);
        return userRepository.save(existingUser);
    }

    private String getEmailFromGitHub(OAuth2UserRequest oAuth2UserRequest) throws IOException {
//        GitHubClient gitHubClient = new GitHubClient(oAuth2UserRequest.getAccessToken().getTokenValue());
//        System.out.println(gitHubClient.getUser());
        String url = "https://api.github.com/user/emails?access_token=" + oAuth2UserRequest.getAccessToken().getTokenValue();
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        Gson gson = new com.google.gson.Gson();
        Type listType = new TypeToken<ArrayList<GitHubEmail>>() {
        }.getType();
        List<GitHubEmail> gitHubEmails = gson.fromJson(json, listType);
        for (GitHubEmail gitHubEmail : gitHubEmails) {
            if (gitHubEmail.isPrimary()) {
                System.out.println(gitHubEmail.getEmail());
                return gitHubEmail.getEmail();
            }
        }
        return null;
    }
}

