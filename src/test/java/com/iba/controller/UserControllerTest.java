/*
package com.iba.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iba.model.project.Lang;
import com.iba.model.user.Contact;
import com.iba.model.user.JobExperience;
import com.iba.model.user.User;
import com.iba.model.user.UserLang;
import com.iba.model.view.Constants;
import com.iba.repository.*;
import com.iba.security.oauth2.jwt.JwtGenerator;
import com.iba.security.oauth2.jwt.JwtUser;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: 11.06.2019 Refactoring by application-test.prop
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("JUnit_username")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource("classpath:/application-test.properties")
@ContextConfiguration
public class UserControllerTest {

    private static final String URL = "/user";
    private static final String contributorUsername = "JUnit_contributor";

    @Value("${access.token.JUnit_contributor}")
    private String accessTokenJUnit_contributor;

    @Value("${refresh.token.JUnit_contributor}")
    private String refreshTokenJUnit_contributor;

    @Value("${access.token.JUnit_username}")
    private String accessTokenJUnit_username;

    @Value("${refresh.token.JUnit_username}")
    private String refreshTokenJUnit_username;

    @Value("${JUnit_username.id}")
    private String JUnit_usernameId;

    @Autowired
    private MockMvc mockMvc;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ContactRepository contactRepository;

    @Resource
    private JobRepository jobRepository;

    @Resource
    private LangRepository langRepository;

    @Resource
    private UserLangRepository userLangRepository;

    @Test
    public void getProfile() throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assert user != null;

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/profile")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(Math.toIntExact(user.getId()))))
                .andExpect(jsonPath("$.username", is(user.getUsername())));
    }

    @Test
    public void getUserProfileSuccess() throws Exception {
        User user = userRepository.findByUsername(contributorUsername);
        assert user != null;

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/page/{username}", contributorUsername)
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(Math.toIntExact(user.getId()))))
                .andExpect(jsonPath("$.username", is(user.getUsername())));

    }

    @Test
    public void getUserProfileFailed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/page/{username}", "123123")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
    }

    // TODO: 09.06.2019 Set new username successfully

    @Test
    public void updateUsernameForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/update-username")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .content("Test failed")
                .accept(MediaType.ALL))
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateUsername423() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/update-username")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .content(contributorUsername)
                .accept(MediaType.ALL))
                .andExpect(status().is(423));
    }

    @Test
    public void updateUser() throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setFirstName("Ivan");
        user.setLastName("Chyzhyk");

        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/update")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());

        User userAfterRequest = userRepository.findByUsername("JUnit_username");
        Assert.assertEquals(userAfterRequest.getFirstName(), "Ivan");
        Assert.assertEquals(userAfterRequest.getLastName(), "Chyzhyk");

        user.setFirstName("JUnitFirstName");
        user.setLastName("JUnitLastName");

        userRepository.save(user);
    }

    @Test
    public void updateUser400() throws Exception {
        User user = new User();
        user.setUsername("JUnit_username");
        user.setFirstName("123 123");
        user.setLastName("123 123");

        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/update")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void addUserLang() throws Exception {
        String levelOfUserLang = "MEDIUM";
        Lang lang = langRepository.findById(180);

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .param("lang_id", String.valueOf(lang.getId()))
                .content(levelOfUserLang)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        UserLang addedUserLang = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<UserLang>() {
        });
        assert addedUserLang != null;
        Assert.assertEquals(addedUserLang.getLang().getLangName(), lang.getLangName());
        Assert.assertEquals(addedUserLang.getLevel(), levelOfUserLang);

        Assert.assertTrue(userLangRepository.findById(addedUserLang.getId()).isPresent());

        userLangRepository.delete(addedUserLang);

        Assert.assertFalse(userLangRepository.findById(addedUserLang.getId()).isPresent());
    }

    @Test
    public void addUserLang404() throws Exception {
        String langIdDoNotExist = "333";
        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .param("lang_id", langIdDoNotExist)
                .content("HIGH")
                .accept(MediaType.ALL))
                .andExpect(status().is(404));
    }

    @Test
    public void changeUserUserLang() throws Exception {
        String oldLevel = "MEDIUM";
        UserLang changeUserLang = new UserLang(langRepository.findById(180), oldLevel, Long.parseLong(JUnit_usernameId));
        userLangRepository.save(changeUserLang);

        Assert.assertTrue(userLangRepository.findById(changeUserLang.getId()).isPresent());

        Lang newLang = langRepository.findById(105);
        String newLevel = "HIGH";

        changeUserLang.setLang(newLang);
        changeUserLang.setLevel(newLevel);
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeUserLang))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        UserLang changedUserLang = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<UserLang>() {
        });

        assert changedUserLang != null;
        Assert.assertEquals(changedUserLang.getLang().getLangName(), newLang.getLangName());
        Assert.assertEquals(changedUserLang.getLevel(), newLevel);

        Assert.assertTrue(userLangRepository.findById(changedUserLang.getId()).isPresent());

        userLangRepository.delete(changedUserLang);

        Assert.assertFalse(userLangRepository.findById(changedUserLang.getId()).isPresent());
    }

    @Test
    public void changeUserLang400() throws Exception {
        UserLang changeUserLang = new UserLang(langRepository.findById(180), RandomString.make(250), Long.parseLong(JUnit_usernameId));
        userLangRepository.save(changeUserLang);

        Assert.assertTrue(userLangRepository.findById(changeUserLang.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeUserLang))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));

        userLangRepository.delete(changeUserLang);

        Assert.assertFalse(userLangRepository.findById(changeUserLang.getId()).isPresent());
    }

    @Test
    public void changeUserLang404() throws Exception {
        String oldLevel = "MEDIUM";
        UserLang changeUserLang = new UserLang(langRepository.findById(180), oldLevel, Long.parseLong(JUnit_usernameId));
        changeUserLang.setId(-1L);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeUserLang))
                .accept(MediaType.ALL))
                .andExpect(status().is(404));

        Assert.assertFalse(userLangRepository.findById(changeUserLang.getId()).isPresent());
    }

    @Test
    public void deleteUserUserLang() throws Exception {
        String oldLevel = "MEDIUM";
        UserLang deleteUserLang = new UserLang(langRepository.findById(180), oldLevel, Long.parseLong(JUnit_usernameId));
        userLangRepository.save(deleteUserLang);

        Assert.assertTrue(userLangRepository.findById(deleteUserLang.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(deleteUserLang))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());

        Assert.assertFalse(jobRepository.findById(deleteUserLang.getId()).isPresent());
    }

    @Test
    public void deleteUserLang400() throws Exception {
        String oldLevel = "MEDIUM";
        UserLang changeUserLang = new UserLang(langRepository.findById(180), oldLevel, Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/userlang")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeUserLang))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void addUserContact() throws Exception {
        String newEmailContact = "ivan.forever91@gmail.com";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .param("type", String.valueOf(Constants.ContactType.EMAIL))
                .content(newEmailContact)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Contact newContact = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Contact>() {
        });
        assert newContact != null;
        Assert.assertEquals(newContact.getContactType(), Constants.ContactType.EMAIL);
        Assert.assertEquals(newContact.getContactValue(), newEmailContact);

        Assert.assertTrue(contactRepository.findById(newContact.getId()).isPresent());

        contactRepository.delete(newContact);

        Assert.assertFalse(contactRepository.findById(newContact.getId()).isPresent());
    }

    @Test
    public void addUserContact400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .param("type", String.valueOf(Constants.ContactType.EMAIL))
                .content(" fasdfasdf  fdsaf asdf")
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void changeUserContact() throws Exception {
        String vkId = "id123123123";
        String oldEmailContact = "ivan.forever91@gmail.com";

        Contact contactToChange = new Contact();
        contactToChange.setContactValue(oldEmailContact);
        contactToChange.setContactType(Constants.ContactType.EMAIL);
        contactToChange.setUserId(Long.parseLong(JUnit_usernameId));
        contactRepository.save(contactToChange);

        Assert.assertTrue(contactRepository.findById(contactToChange.getId()).isPresent());

        contactToChange.setContactType(Constants.ContactType.VK);
        contactToChange.setContactValue(vkId);
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(contactToChange))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Contact changedContact = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Contact>() {
        });

        assert changedContact != null;
        Assert.assertEquals(changedContact.getContactType(), Constants.ContactType.VK);
        Assert.assertEquals(changedContact.getContactValue(), vkId);

        Assert.assertTrue(contactRepository.findById(contactToChange.getId()).isPresent());

        contactRepository.delete(changedContact);

        Assert.assertFalse(contactRepository.findById(changedContact.getId()).isPresent());
    }

    @Test
    public void changeUserContact400() throws Exception {
        String failedValidation = "posajd foiasdo[ifm [oaisdm";

        Contact contactToChange = new Contact();
        contactToChange.setContactValue(failedValidation);
        contactToChange.setContactType(Constants.ContactType.EMAIL);
        contactToChange.setUserId(Long.parseLong(JUnit_usernameId));

        contactRepository.save(contactToChange);

        Assert.assertTrue(contactRepository.findById(contactToChange.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(contactToChange))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));

        Assert.assertTrue(contactRepository.findById(contactToChange.getId()).isPresent());

        contactRepository.delete(contactToChange);

        Assert.assertFalse(contactRepository.findById(contactToChange.getId()).isPresent());
    }

    @Test
    public void changeUserContact404() throws Exception {
        String oldEmailContact = "ivan.forever91@gmail.com";

        Contact contactToChange = new Contact();
        contactToChange.setContactValue(oldEmailContact);
        contactToChange.setContactType(Constants.ContactType.EMAIL);
        contactToChange.setUserId(Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(contactToChange))
                .accept(MediaType.ALL))
                .andExpect(status().is(404));
    }

    @Test
    public void deleteUserContact() throws Exception {
        String oldEmailContact = "ivan.forever91@gmail.com";

        Contact contactToDelete = new Contact();
        contactToDelete.setContactValue(oldEmailContact);
        contactToDelete.setContactType(Constants.ContactType.EMAIL);
        contactToDelete.setUserId(Long.parseLong(JUnit_usernameId));
        contactRepository.save(contactToDelete);
        Assert.assertTrue(contactRepository.findById(contactToDelete.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(contactToDelete))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());

        Assert.assertFalse(contactRepository.findById(contactToDelete.getId()).isPresent());
    }

    @Test
    public void deleteUserContact400() throws Exception {
        String oldEmailContact = "ivan.forever91@gmail.com";

        Contact contactToChange = new Contact();
        contactToChange.setContactValue(oldEmailContact);
        contactToChange.setContactType(Constants.ContactType.EMAIL);
        contactToChange.setUserId(Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/contact")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(contactToChange))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void addUserJobExperience() throws Exception {
        JobExperience newJobExperience = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newJobExperience))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        JobExperience jobExperience = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<JobExperience>() {
        });
        assert jobExperience != null;
        Assert.assertEquals(jobExperience.getWorkingPeriod(), newJobExperience.getWorkingPeriod());
        Assert.assertEquals(jobExperience.getWorkPlace(), newJobExperience.getWorkPlace());
        Assert.assertEquals(jobExperience.getPosition(), newJobExperience.getPosition());
        Assert.assertEquals(jobExperience.getActivity(), newJobExperience.getActivity());

        Assert.assertTrue(jobRepository.findById(jobExperience.getId()).isPresent());

        jobRepository.delete(jobExperience);


        Assert.assertFalse(jobRepository.findById(jobExperience.getId()).isPresent());
    }

    @Test
    public void addUserJobExperience400() throws Exception {
        JobExperience newJobExperience = new JobExperience(RandomString.make(100), "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/add/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newJobExperience))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void changeUserJobExperience() throws Exception {
        String newPosition = "Middle Java Developer";
        String newActivity = "Write new features";

        JobExperience jobExperienceToChange = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));
        jobRepository.save(jobExperienceToChange);

        Assert.assertTrue(jobRepository.findById(jobExperienceToChange.getId()).isPresent());

        jobExperienceToChange.setPosition(newPosition);
        jobExperienceToChange.setActivity(newActivity);
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(jobExperienceToChange))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        JobExperience changedJobExperience = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<JobExperience>() {
        });

        assert changedJobExperience != null;
        Assert.assertEquals(changedJobExperience.getPosition(), newPosition);
        Assert.assertEquals(changedJobExperience.getActivity(), newActivity);

        Assert.assertTrue(jobRepository.findById(changedJobExperience.getId()).isPresent());

        jobRepository.delete(changedJobExperience);

        Assert.assertFalse(jobRepository.findById(changedJobExperience.getId()).isPresent());
    }

    @Test
    public void changeUserJobExperience404() throws Exception {
        JobExperience newJobExperience = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newJobExperience))
                .accept(MediaType.ALL))
                .andExpect(status().is(404));
    }

    @Test
    public void changeUserJobExperience400() throws Exception {
        JobExperience newJobExperience = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));
        jobRepository.save(newJobExperience);

        Assert.assertTrue(jobRepository.findById(newJobExperience.getId()).isPresent());

        newJobExperience.setWorkingPeriod(RandomString.make(100));
        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/edit/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newJobExperience))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));

        Assert.assertTrue(jobRepository.findById(newJobExperience.getId()).isPresent());

        jobRepository.delete(newJobExperience);

        Assert.assertFalse(jobRepository.findById(newJobExperience.getId()).isPresent());
    }

    @Test
    public void deleteUserJobExperience() throws Exception {
        JobExperience jobExperienceToChange = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));
        jobRepository.save(jobExperienceToChange);

        Assert.assertTrue(jobRepository.findById(jobExperienceToChange.getId()).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(jobExperienceToChange))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());

        Assert.assertFalse(jobRepository.findById(jobExperienceToChange.getId()).isPresent());
    }

    @Test
    public void deleteUserJobExperience400() throws Exception {
        JobExperience newJobExperience = new JobExperience("03.01.2019-00.00.00", "Iba", "Java Developer", "WRITE FUCKING TEST!!!", Long.parseLong(JUnit_usernameId));

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/delete/job")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(newJobExperience))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void changePassword() throws Exception {
        String oldPassword = "123123";

        User userWithNewPassword = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        assert userWithNewPassword != null;

        userWithNewPassword.setPassword(oldPassword);
        userWithNewPassword.setRepeatPassword(oldPassword);
        userWithNewPassword.setOldPassword(oldPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/change-pass")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userWithNewPassword))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword421() throws Exception {
        String oldPassword = "123";

        User userWithNewPassword = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        assert userWithNewPassword != null;

        userWithNewPassword.setPassword(oldPassword);
        userWithNewPassword.setRepeatPassword(oldPassword);
        userWithNewPassword.setOldPassword(oldPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/change-pass")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userWithNewPassword))
                .accept(MediaType.ALL))
                .andExpect(status().is(421));
    }

    @Test
    public void changePassword422() throws Exception {
        String oldPassword = "123123";
        String incorrectOldPassword = "123123123";

        User userWithNewPassword = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        assert userWithNewPassword != null;

        userWithNewPassword.setPassword(oldPassword);
        userWithNewPassword.setRepeatPassword(oldPassword);
        userWithNewPassword.setOldPassword(incorrectOldPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/change-pass")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userWithNewPassword))
                .accept(MediaType.ALL))
                .andExpect(status().is(422));
    }

    @Test
    public void changePassword423() throws Exception {
        String oldPassword = "123123";
        String incorrectRepeatPassword = "123123123";

        User userWithNewPassword = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        assert userWithNewPassword != null;

        userWithNewPassword.setPassword(oldPassword);
        userWithNewPassword.setRepeatPassword(incorrectRepeatPassword);
        userWithNewPassword.setOldPassword(oldPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/change-pass")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userWithNewPassword))
                .accept(MediaType.ALL))
                .andExpect(status().is(423));
    }

}
*/
