package com.iba.service;

import com.iba.exceptions.*;
import com.iba.model.user.User;
import com.iba.repository.UserRepository;
import com.iba.utils.PasswordEncoderMD5;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final PasswordEncoderMD5 passwordEncoderMD5;

    private final ValidatorService validatorService;

    private final MailService mailService;

    private final UserRepository userRepository;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(UserService.class);

    // TODO: 24.09.2019 Edit server address
    @Value("${server.address}")
    private String serverAddress;

    @Autowired
    public UserService(PasswordEncoderMD5 passwordEncoderMD5, ValidatorService validatorService, MailService mailService, UserRepository userRepository) {
        this.passwordEncoderMD5 = passwordEncoderMD5;
        this.validatorService = validatorService;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    /**
     * Update user's First and Last names.
     *
     * @param user     - authenticated User
     * @param editUser - user with new names.
     */
    public void updateUser(User user, User editUser) {
        if (!validatorService.validateFirstName(editUser.getFirstName()) || !validatorService.validateLastName(editUser.getLastName())) {
            throw new Exception_400("Bad credentials");
        }
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        userRepository.save(user);
    }

    /**
     * Registration new user.
     *
     * @param user - new user
     * @throws NoSuchAlgorithmException if method of encode not found
     */
    public void registrationUser(User user) throws NoSuchAlgorithmException {
        user.setPassword(passwordEncoderMD5.createPassword(user.getPassword()));
        user.setCreationDate();
        userRepository.save(user);
    }

    /**
     * Set activation code in user.
     *
     * @param code - activation code
     * @return true if user activated
     */
    public boolean activateUser(String code) {
        User user = userRepository.findByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception_404("Email not found " + email);
        }
        return user;
    }

    /**
     * Change password.
     *
     * @param user - user with old password
     * @param usr  - user with new password
     * @throws NoSuchAlgorithmException if method of encode not found
     */
    public void changePassword(User user, User usr) throws NoSuchAlgorithmException {
        if (!validatorService.validatePassword(usr.getOldPassword()) || !validatorService.validatePassword(usr.getPassword()) || !validatorService.validatePassword(usr.getRepeatPassword())) {
            throw new Exception_421("Bad credentials");
        }
        if (!passwordEncoderMD5.createPassword(usr.getOldPassword()).equals(user.getPassword())) {
            throw new Exception_422("Incorrect password");
        }
        if (!usr.getPassword().equals(usr.getRepeatPassword())) {
            throw new Exception_423("New passwords aren't equals");
        }
        user.setPassword(passwordEncoderMD5.createPassword(usr.getPassword()));
        userRepository.save(user);
    }

    /**
     * Update user photo.
     *
     * @param user     - - user with old profile photo
     * @param editUser - user with new profile photo
     */
    public void updateUserAvatar(User user, User editUser) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        File file = new File("temp/images/" + filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = Base64.decodeBase64(editUser.getAvatar().substring(22));
            fos.write(data);
        } catch (IOException e) {
            throw new Exception_400("Bad photo");
        }
        File deleteFile = new File("temp/images/" + user.getProfilePhoto());
        if (!deleteFile.delete()) {
            logger.debug("File was not deleted");
        }
        user.setProfilePhoto(filename);
        userRepository.save(user);
    }

    /**
     * Send activation message on user email.
     *
     * @param user - user to send
     */
    public void sendActivationLinkToEmail(User user) {
        user.setActivationCode(UUID.randomUUID().toString());
        String message = "Hello " + user.getUsername() + "\t\tNice to meet you!\tPlease, visit this link to " +
                "activate your account: " + serverAddress + "/user/activate/" + user.getActivationCode();
        mailService.send(user.getEmail(), "Activate account", message);
    }

    // TODO: 01.08.2019 Refactoring
    public void sendModeratorActivationLinkToEmail(User user, String activationCode, String glossaryName, Long id) {
        String message = "Hello " + user.getUsername() + "\t\tNice to meet you!\tPlease, visit this link to " +
                "be Moderator in this Glossary: " + glossaryName + "\t" + serverAddress + "/glossaries/" + id + "/groups?activate=" + activationCode;
        mailService.send(user.getEmail(), "Moderator", message);
    }
}
