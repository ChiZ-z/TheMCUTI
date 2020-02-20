package com.iba.controller;

import com.iba.exceptions.*;
import com.iba.model.chart.ResultStat;
import com.iba.model.project.Lang;
import com.iba.model.user.Contact;
import com.iba.model.user.JobExperience;
import com.iba.model.user.User;
import com.iba.model.user.UserLang;
import com.iba.model.view.Constants;
import com.iba.repository.*;
import com.iba.security.TokenProvider;
import com.iba.security.oauth2.jwt.JwtUser;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import com.iba.service.UserService;
import com.iba.service.ValidatorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final HistoryService historyService;

    private final UserLangRepository userLangRepository;

    private final JobRepository jobRepository;

    private final ContactRepository contactRepository;

    private final LangRepository langRepository;

    private final ValidatorService validatorService;

    private final TokenProvider tokenProvider;

    private final AccessService accessService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(UserController.class);

    @Value("${file.path.image}")
    private String uploadPath;

    public UserController(UserService userService, UserRepository userRepository,
                          HistoryService historyService, UserLangRepository userLangRepository,
                          JobRepository jobRepository, ContactRepository contactRepository,
                          LangRepository langRepository, ValidatorService validatorService,
                          TokenProvider tokenProvider, AccessService accessService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.historyService = historyService;
        this.userLangRepository = userLangRepository;
        this.jobRepository = jobRepository;
        this.contactRepository = contactRepository;
        this.langRepository = langRepository;
        this.validatorService = validatorService;
        this.tokenProvider = tokenProvider;
        this.accessService = accessService;
    }

    /**
     * Get information about User.
     *
     * @param user authenticated User
     * @return User with profile info
     */

    @GetMapping("/profile")
    public User getUser(@AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " go to profile");
        user.setResultStat(historyService.getAllUserStats(user));
        user.setPassword(null);
        user.setRefreshToken(null);
        return user;
    }

    /**
     * Get User by username.
     *
     * @param username contributor's username
     * @return User
     * @throws Exception_404 if User not found
     */
    @GetMapping("/page/{username}")
    public User getUserByUsername(@PathVariable("username") String username) {
        User user = userRepository.findByUsername(username);
        accessService.isNotObject(user);
        logger.debug("User visit other " + user.getUsername() + " profile");
        user.setResultStat(historyService.getAllUserStats(user));
        user.setPassword(null);
        user.setRefreshToken(null);
        return user;
    }

    /**
     * Set new username of User.
     *
     * @param user        authenticated User
     * @param newUsername new username
     * @return tokens with new username
     * @throws Exception_403 if validation of the username failed
     * @throws Exception_423 if User with this username exists
     */
    @PostMapping("/update-username")
    public Map<String, String> updateUsername(@AuthenticationPrincipal User user, @RequestBody String newUsername) {
        logger.debug(user.getUsername() + " change username on " + newUsername);
        HashMap<String, String> token = new HashMap<>();
        if (!validatorService.validateUsername(newUsername)) {
            throw new Exception_403("Bad credentials");
        }
        if (userRepository.countAllByUsername(newUsername) > 0) {
            throw new Exception_423("User with such username exists");
        }
        user.setUsername(newUsername);
        JwtUser jwtUser = new JwtUser(user.getId(), newUsername, new Date().getTime());
        String newRefresh = tokenProvider.generateRefresh(jwtUser);
        user.setRefreshToken(newRefresh);
        userRepository.save(user);
        token.put("Token", tokenProvider.generateAccess(jwtUser));
        token.put("Refresh", newRefresh);
        return token;
    }

    /**
     * Update User's First and Last names.
     *
     * @param user     authenticated User
     * @param editUser User with new names.
     * @throws Exception_400 if validation of the editUser failed
     */
    @PutMapping("/update")
    public void updateUser(@AuthenticationPrincipal User user, @RequestBody User editUser) {
        logger.debug(user.getUsername() + " change firstname on " + editUser.getFirstName() + " and lastname on " + editUser.getLastName());
        userService.updateUser(user, editUser);
    }

    /**
     * Update User photo.
     *
     * @param user     User with old profile photo
     * @param editUser User with new profile photo
     * @return User with new photo
     * @throws Exception_400 if new avatar parse failed
     */
    @PostMapping("/{userId}/avatar")
    public User updateAvatar(@PathVariable("userId") User user, @RequestBody User editUser) {
        logger.debug(user.getUsername() + " change avatar");
        if (new File(uploadPath).mkdirs()) {
            logger.debug("Directory was created");
        }
        userService.updateUserAvatar(user, editUser);
        user.setResultStat(historyService.getAllUserStats(user));
        return user;
    }

    /**
     * Activate User email.
     *
     * @param code activation code
     * @return User activated
     */
    @GetMapping("/activate/{code}")
    public boolean activate(@PathVariable String code) {
        return userService.activateUser(code);
    }

    /**
     * Get statistic of User in Project.
     *
     * @param user      authenticated User
     * @param projectId Project's id for stats
     * @return stats of User in Project
     */
    @GetMapping("/stats/project/{projectId}")
    public ResultStat getPersonalStats(@AuthenticationPrincipal User user, @PathVariable("projectId") long projectId) {
        logger.debug("User " + user.getUsername() + " get personal statistic");
        return historyService.getAllUserStatsInProject(user, projectId);
    }

    /**
     * Add UserLang.
     *
     * @param user    authenticated User
     * @param lang_id id of Lang in UserLang
     * @param level   level of UserLang
     * @return new UserLang
     * @throws Exception_404 if UserLang exists
     * @throws Exception_400 if validation of the UserLang failed
     */
    @PostMapping("/add/userlang")
    public UserLang addUserLang(@AuthenticationPrincipal User user, @RequestParam long lang_id, @RequestBody String level) {
        logger.debug("User " + user.getUsername() + " add to delete user lang");
        Lang lang = langRepository.findById(lang_id);
        if (lang == null || user.getLangs().stream().anyMatch(a -> a.getLang().getId() == lang_id) || level == null) {
            logger.debug("User lang is null");
            throw new Exception_404("Lang exists");
        }
        UserLang userLang = new UserLang();
        userLang.setLevel(level);
        userLang.setLang(lang);
        userLang.setUserId(user.getId());
        if (!validatorService.validateLevelValue(userLang.getLevel())) {
            logger.debug("User lang validation failed");
            throw new Exception_400("Bad credentials ");
        }
        userLangRepository.save(userLang);
        return userLang;
    }

    /**
     * Change UserLang.
     *
     * @param user     authenticated User
     * @param userLang UserLang for change
     * @return updated UserLang
     * @throws Exception_404 if UserLang not found or UserLang don't contains this lang
     * @throws Exception_400 if validation of the UserLang failed
     */
    @PostMapping("/edit/userlang")
    public UserLang changeUserLang(@AuthenticationPrincipal User user, @RequestBody UserLang userLang) {
        logger.debug("User " + user.getUsername() + " change user lang");
        if (userLang == null || !user.getLangs().contains(userLang)) {
            logger.debug("User lang is empty");
            throw new Exception_404("User lang not found or user don't contain this lang");
        }
        if (!validatorService.validateLevelValue(userLang.getLevel())) {
            logger.debug("Validation user lang failed");
            throw new Exception_400("Bad credentials");
        }
        userLangRepository.save(userLang);
        return userLang;
    }

    /**
     * Delete UserLang.
     *
     * @param user     authenticated User
     * @param userLang UserLang for delete
     * @throws Exception_400 if UserLang not found or UserLang don't contain this lang
     */
    @PostMapping("/delete/userlang")
    public void deleteUserLang(@AuthenticationPrincipal User user, @RequestBody UserLang userLang) {
        logger.debug("User " + user.getUsername() + " try to delete user lang");
        if (userLang == null || !user.getLangs().contains(userLang)) {
            logger.debug("User lang is null");
            throw new Exception_400("User lang not found or user dont contain this lang");
        }
        userLangRepository.delete(userLang);
    }

    /**
     * Add JobExperience.
     *
     * @param user          authenticated User
     * @param jobExperience new JobExperience
     * @return new JobExperience
     * @throws Exception_400 if validation of the JobExperience failed
     */
    @PostMapping("/add/job")
    public JobExperience addUserJob(@AuthenticationPrincipal User user, @RequestBody JobExperience jobExperience) {
        logger.debug("User " + user.getUsername() + " try to add user job");
        accessService.isNotObject(jobExperience);
        if (!validatorService.validateCompanyValue(jobExperience.getWorkPlace()) || !validatorService.validatePositionValue(jobExperience.getPosition())
                || !validatorService.validatePeriodValue(jobExperience.getWorkingPeriod()) || !validatorService.validateActivityValue(jobExperience.getActivity())) {
            logger.debug("Job validation failed");
            throw new Exception_400("Bad credentials ");
        }
        jobExperience.setUserId(user.getId());
        jobRepository.save(jobExperience);
        return jobExperience;
    }

    /**
     * Change JobExperience.
     *
     * @param user          authenticated User
     * @param jobExperience user JobExperience for change
     * @return updated JobExperience
     * @throws Exception_404 if JobExperience not found or User don't contain this JobExperience
     * @throws Exception_400 if validation of the JobExperience failed
     */
    @PostMapping("/edit/job")
    public JobExperience changeUserJob(@AuthenticationPrincipal User user, @RequestBody JobExperience jobExperience) {
        logger.debug("User " + user.getUsername() + " change user job");
        if (jobExperience == null || !user.getJobs().contains(jobExperience)) {
            logger.debug("Job is null");
            throw new Exception_404("Job not found or user don't contain this job");
        }
        if (!validatorService.validateCompanyValue(jobExperience.getWorkPlace()) || !validatorService.validatePositionValue(jobExperience.getPosition())
                || !validatorService.validatePeriodValue(jobExperience.getWorkingPeriod()) || !validatorService.validateActivityValue(jobExperience.getActivity())) {
            logger.debug("Job validation failed");
            throw new Exception_400("Bad credentials");
        }
        jobRepository.save(jobExperience);
        return jobExperience;
    }

    /**
     * Delete JobExperience.
     *
     * @param user          authenticated User
     * @param jobExperience JobExperience for delete
     * @throws Exception_400 if JobExperience not found or User don't contain this JobExperience
     */
    @PostMapping("/delete/job")
    public void deleteUserJob(@AuthenticationPrincipal User user, @RequestBody JobExperience jobExperience) {
        logger.debug("User " + user.getUsername() + " try to delete user job");
        if (jobExperience == null || !user.getJobs().contains(jobExperience)) {
            logger.debug("Job is null");
            throw new Exception_400("Job not found or user dont contain this job");
        }
        jobRepository.delete(jobExperience);
    }

    /**
     * Add Contact.
     *
     * @param user    authenticated User
     * @param contact text in Contact
     * @param type    type of Contact
     * @return new Contact
     * @throws Exception_400 if validation of the Contact failed
     * @throws Exception_404 if Contact not found
     */
    @PostMapping("/add/contact")
    public Contact addUserContact(@AuthenticationPrincipal User user, @RequestBody String contact, @RequestParam Constants.ContactType type) {
        logger.debug("User " + user.getUsername() + " try to add user contact");
        accessService.isNotObject(contact);
        if (!validatorService.validateContactValue(contact)) {
            logger.debug("Contact validation failed");
            throw new Exception_400("Bad credentials");
        }
        Contact cont = new Contact();
        cont.setContactType(type);
        cont.setContactValue(contact);
        cont.setUserId(user.getId());
        contactRepository.save(cont);
        return cont;
    }

    /**
     * Change Contact.
     *
     * @param user    authenticated User
     * @param contact Contact for change
     * @return updated Contact
     * @throws Exception_404 if Contact not found or User don't contain this Contact
     */
    @PostMapping("/edit/contact")
    public Contact changeUserContact(@AuthenticationPrincipal User user, @RequestBody Contact contact) {
        logger.debug("User " + user.getUsername() + " change user contact");
        if (contact == null || !user.getContacts().contains(contact)) {
            logger.debug("Contact is null");
            throw new Exception_404("Contact not found or user dont contain this contact");
        }
        if (!validatorService.validateContactValue(contact.getContactValue())) {
            logger.debug("Contact validation failed");
            throw new Exception_400("Bad credentials");
        }
        contactRepository.save(contact);
        return contact;
    }

    /**
     * Delete Contact.
     *
     * @param user    authenticated User
     * @param contact Contact for delete
     * @throws Exception_400 if Contact not found or User don't contain this Contact
     */
    @PostMapping("/delete/contact")
    public void deleteUserContact(@AuthenticationPrincipal User user, @RequestBody Contact contact) {
        logger.debug("User " + user.getUsername() + " try to delete user contact");
        if (contact == null || !user.getContacts().contains(contact)) {
            logger.debug("Contact is null");
            throw new Exception_400("Contact not found or user dont contain this contact");
        }
        contactRepository.delete(contact);
    }

    /**
     * Change password.
     *
     * @param user authenticated User
     * @param usr  user with new password
     * @throws NoSuchAlgorithmException if method of encode not found
     * @throws Exception_421            if validation of the old, new or repeat password failed
     * @throws Exception_422            if old password not equal new password
     * @throws Exception_423            if new password not equal repeat password
     */
    @PostMapping("/change-pass")
    public void changePassword(@AuthenticationPrincipal User user, @RequestBody User usr) throws NoSuchAlgorithmException {
        logger.debug("User " + user.getUsername() + " change password");
        userService.changePassword(user, usr);
    }

    /**
     * Drop profile photo.
     *
     * @param user authenticated User
     * @throws Exception_400 if drop User photo failed
     */
    @DeleteMapping("drop-avatar")
    public void dropAvatar(@AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " delete avatar");
        File file = new File(uploadPath + user.getProfilePhoto());
        if (file.delete()) {
            user.setProfilePhoto(null);
            userRepository.save(user);
        } else {
            logger.debug("User " + user.getUsername() + " delete avatar failed");
            throw new Exception_400("Error while deleting photo");
        }
    }

    /**
     * Update email.
     *
     * @param user     authenticated User
     * @param newEmail new email
     * @throws Exception_400 if validation of the email failed or User with this email exists
     */
    @PostMapping("/update-email")
    public void updateEmail(@AuthenticationPrincipal User user, @RequestBody String newEmail) {
        logger.debug("User " + user.getUsername() + " try to update user email");
        if (!validatorService.validateEmail(newEmail)) {
            logger.info("Email validation failed");
            throw new Exception_400("Bad credentials");
        }
        if (userRepository.countAllByEmail(newEmail) > 0) {
            logger.info("New email exist");
            throw new Exception_400("User with such email exists");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    /**
     * Get creation date of User
     *
     * @param user User
     * @return creation date (yyyy-MM-dd) of User
     */
    @GetMapping("/creation-date")
    public Timestamp getCreationDateFromUser(@AuthenticationPrincipal User user) {
        return user.getCreationDate();
    }

}

