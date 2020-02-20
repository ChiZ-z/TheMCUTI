/*
package com.iba.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iba.model.user.User;
import com.iba.repository.UserRepository;
import com.iba.security.oauth2.jwt.JwtGenerator;
import com.iba.security.oauth2.jwt.JwtUser;
import com.iba.security.oauth2.jwt.JwtValidator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: 11.06.2019 Refactoring by application-test.prop add new users onlu for authController
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource("classpath:/application-test.properties")
@ContextConfiguration
public class AuthControllerTest {

    private static final String URL = "/auth";
    private static final String freeUsername = "qwerty222";
    private static final String freeEmail = "ivan@mail.tut";
    private static final String constantUsername = "JUnit_username";
    private static final String constantEmail = "JUnit@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Resource
    private UserRepository userRepository;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private JwtValidator jwtValidator;

    @Test
    public void registration() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Map<String, String> tokens = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Map<String, String>>() {
        });

        assert tokens.containsKey("Token");
        assert tokens.containsKey("Refresh");

        JwtUser jwtUserAccess = jwtValidator.validateAccess(tokens.get("Token"));
        JwtUser jwtUserRefresh = jwtValidator.validateRefresh(tokens.get("Refresh"));

        assert jwtUserAccess != null;
        assert jwtUserRefresh != null;
        assertEquals(jwtUserAccess.getUserName(), registrationUser.getUsername());
        assertEquals(jwtUserRefresh.getUserName(), registrationUser.getUsername());

        assertNotNull(userRepository.findByUsername(registrationUser.getUsername()));

        userRepository.delete(userRepository.findByUsername(registrationUser.getUsername()));

        assertNull(userRepository.findByUsername(registrationUser.getUsername()));
    }

    @Test
    public void registration400() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");
        registrationUser.setUsername("Chyzhyk d d");
        registrationUser.setEmail("JU");
        registrationUser.setFirstName("ddddIvan");

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));

        assertNull(userRepository.findByUsername(registrationUser.getUsername()));
    }

    @Test
    public void registration424() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");
        registrationUser.setPassword("123123");
        registrationUser.setRepeatPassword("123123123");

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(424));

        assertNull(userRepository.findByUsername(registrationUser.getUsername()));
    }

    @Test
    public void registration421() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");
        registrationUser.setUsername(constantUsername);
        registrationUser.setEmail(constantEmail);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(421));

        assertNotNull(userRepository.findByUsername(constantUsername));
        assertNotNull(userRepository.findByEmail(constantEmail));
    }

    @Test
    public void registration422() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");
        registrationUser.setUsername(constantUsername);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(422));

        assertNotNull(userRepository.findByUsername(constantUsername));
    }

    @Test
    public void registration423() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");
        registrationUser.setEmail(constantEmail);

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(423));

        assertNotNull(userRepository.findByEmail(constantEmail));
    }

    @Test
    public void login() throws Exception {
        User registrationUser = new User(constantEmail, constantUsername, "123123", "JUnitFirstName", "JUnitLastName", "123123");

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Map<String, String> tokens = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Map<String, String>>() {
        });

        assert tokens.containsKey("Token");
        assert tokens.containsKey("Refresh");

        JwtUser jwtUserAccess = jwtValidator.validateAccess(tokens.get("Token"));
        JwtUser jwtUserRefresh = jwtValidator.validateRefresh(tokens.get("Refresh"));

        assert jwtUserAccess != null;
        assert jwtUserRefresh != null;

        assertEquals(jwtUserAccess.getUserName(), registrationUser.getUsername());
        assertEquals(jwtUserRefresh.getUserName(), registrationUser.getUsername());

        assertNotNull(userRepository.findByUsername(registrationUser.getUsername()));
    }

    @Test
    public void login400() throws Exception {
        User registrationUser = new User("JU@gmail.com", "Chyzhyk", "123123", "Ivan", "Chyzhyk", "123123");

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registrationUser))
                .accept(MediaType.ALL))
                .andExpect(status().is(400));

        assertNull(userRepository.findByUsername(registrationUser.getUsername()));
    }

    @Test
    public void refreshTokens() throws Exception {
        User user = userRepository.findByUsername(constantUsername);

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/refresh-token")
                .param("access", jwtGenerator.generateAccess(new JwtUser(user.getId(), user.getUsername(), new Date().getTime())))
                .param("refresh", user.getRefreshToken())
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Map<String, String> tokens = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Map<String, String>>() {
        });

        assert tokens.containsKey("Token");
        assert tokens.containsKey("Refresh");

        JwtUser jwtUserAccess = jwtValidator.validateAccess(tokens.get("Token"));
        JwtUser jwtUserRefresh = jwtValidator.validateRefresh(tokens.get("Refresh"));

        assert jwtUserAccess != null;
        assert jwtUserRefresh != null;

        assertEquals(jwtUserAccess.getUserName(), user.getUsername());
        assertEquals(jwtUserRefresh.getUserName(), user.getUsername());

        assertNotNull(userRepository.findByUsername(user.getUsername()));
    }

    @Test
    public void refreshTokens400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/refresh-token")
                .param("access", "")
                .param("refresh", "")
                .accept(MediaType.ALL))
                .andExpect(status().is(400));
    }

    @Test
    public void checkUsernameSuccess() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-username")
                .param("username", freeUsername)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "true");
    }

    @Test
    public void checkUsernameFailed() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-username")
                .param("username", constantUsername)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "false");
    }

    @Test
    public void checkEmailSuccess() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-email")
                .param("email", freeEmail)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "true");
    }

    @Test
    public void checkEmailFailed() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-email")
                .param("email", constantEmail)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "false");
    }

    @Test
    public void checkPassword_1() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-password")
                .param("password", "123")
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "1");
    }

    @Test
    public void checkPassword_2() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-password")
                .param("password", "123123123")
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "2");
    }

    @Test
    public void checkPassword_3() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-password")
                .param("password", "123123123123")
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "3");
    }

    @Test
    public void checkPassword_4() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/check-password")
                .param("password", "123123123123123123")
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(response, "4");
    }

}
*/
