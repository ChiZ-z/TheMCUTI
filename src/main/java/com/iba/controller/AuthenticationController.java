package com.iba.controller;

import com.iba.exceptions.*;
import com.iba.model.user.User;
import com.iba.repository.UserRepository;
import com.iba.security.TokenProvider;
import com.iba.security.oauth2.jwt.JwtUser;
import com.iba.service.UserService;
import com.iba.service.ValidatorService;
import com.iba.utils.PasswordEncoderMD5;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(AuthenticationController.class);

    private final PasswordEncoderMD5 passwordEncoderMD5;

    private final UserRepository userRepository;

    private final UserService userService;

    private final ValidatorService validatorService;

    private final TokenProvider tokenProvider;

    @Autowired
    public AuthenticationController(PasswordEncoderMD5 passwordEncoderMD5, UserRepository userRepository,
                                    UserService userService, ValidatorService validatorService, TokenProvider tokenProvider) {
        this.passwordEncoderMD5 = passwordEncoderMD5;
        this.userRepository = userRepository;
        this.userService = userService;
        this.validatorService = validatorService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registration new User.
     *
     * @param user new User
     * @return map with new tokens
     * @throws NoSuchAlgorithmException if creating of new password failed
     * @throws Exception_400            if validation of the User fields failed
     * @throws Exception_421            if user with this username and password exists
     * @throws Exception_422            if user with this username exists
     * @throws Exception_423            if user with email exists
     * @throws Exception_424            if password and repeat password field are not equal
     */
    @PostMapping("/registration")
    public HashMap<String, String> registration(@RequestBody User user) throws NoSuchAlgorithmException {
        logger.debug("Try to register new user");
        if (!validatorService.validateUser(user)) {
            logger.debug("Registration failed bec validation failed");
            throw new Exception_400("Validation failed.");
        }
        if (!user.getPassword().equals(user.getRepeatPassword())) {
            logger.debug("Password and RepeatPassword are not equal");
            throw new Exception_424("Passwords aren't equals.");
        }
        User usernameUser = userRepository.findByUsername(user.getUsername());
        User emailUser = userRepository.findByEmail(user.getEmail());
        if (emailUser == null && usernameUser == null) {
            userService.registrationUser(user);
            User newUser = userRepository.findByUsername(user.getUsername());
            return setToken(newUser);
        }
        logger.error("User with such username or email exists.");
        if (emailUser != null && usernameUser != null) {
            throw new Exception_421("User with such username and email exists.");
        } else if (usernameUser != null) {
            throw new Exception_422("User with such username exists.");
        } else {
            throw new Exception_423("User with such email exists.");
        }
    }

    /**
     * Login User.
     *
     * @param user login User
     * @return map with new tokens
     * @throws NoSuchAlgorithmException if creating of new password failed
     * @throws Exception_400            if incorrect email, username or password
     */
    @PostMapping("/login")
    public HashMap<String, String> login(@RequestBody User user) throws NoSuchAlgorithmException {
        logger.debug("Try to authenticate exist user");
        User existUser = userRepository.findByUsername(user.getUsername()) != null ?
                userRepository.findByUsername(user.getUsername()) :
                userRepository.findByEmail(user.getUsername());
        if (existUser != null && existUser.getPassword().equals(passwordEncoderMD5.createPassword(user.getPassword())) &&
                (existUser.getUsername().equals(user.getUsername()) || existUser.getEmail().equals(user.getUsername()))) {
            return setToken(existUser);
        }
        logger.error("Incorrect username, or email, or password");
        throw new Exception_400("Incorrect login or password.");
    }

    /**
     * Get new access and refresh token,
     * when token time is up. Set new refresh token
     * in user.
     *
     * @param access  access token
     * @param refresh refresh token
     * @return map with tokens
     * @throws Exception_400 if validation of the token failed
     */
    @GetMapping("/refresh-token")
    public HashMap<String, String> getTokens(@RequestParam String access, @RequestParam String refresh) {
        JwtUser ac = tokenProvider.validateAccess(access);
        JwtUser re = tokenProvider.validateRefresh(refresh);
        if (ac != null || re != null) {
            User existUser = userRepository.findByUsername(re.getUserName());
            return setToken(existUser);
        }
        logger.debug("Validation token failed");
        throw new Exception_400("Validation token failed");
    }

    /**
     * Set tokens in the map.
     *
     * @param user exists User
     * @return map with tokens
     */
    private HashMap<String, String> setToken(User user) {
        logger.debug("User " + user.getUsername() + " get tokens");
        HashMap<String, String> token = new HashMap<>();
        JwtUser jwtUser = new JwtUser(user.getId(), user.getUsername(), new Date().getTime());
        String newRefresh = tokenProvider.generateRefresh(jwtUser);
        user.setRefreshToken(newRefresh);
        userRepository.save(user);
        token.put("Token", tokenProvider.generateAccess(jwtUser));
        token.put("Refresh", newRefresh);
        return token;
    }

    /**
     * Check User with username exists.
     *
     * @param username new username
     * @return false if username exist
     */
    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam String username) {
        return userRepository.countAllByUsername(username) == 0;
    }

    /**
     * Check User with email exists.
     *
     * @param email new email
     * @return false if email exist
     */
    @GetMapping("/check-email")
    public boolean checkEmail(@RequestParam String email) {
        return userRepository.countAllByEmail(email) == 0;
    }

    /**
     * Validation of new password.
     *
     * @param password new password
     * @return 1 if length < 6 ,2 if length < 11, 3 if length < 16, else 4
     */
    @GetMapping("/check-password")
    public int checkPassword(@RequestParam String password) {
        if (password.length() < 6) return 1;
        else if (password.length() < 11) return 2;
        else if (password.length() < 16) return 3;
        else return 4;
    }
}
