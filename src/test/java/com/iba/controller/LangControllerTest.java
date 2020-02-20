/*
package com.iba.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iba.model.project.Lang;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.user.User;
import com.iba.repository.LangRepository;
import com.iba.repository.ProjectRepository;
import com.iba.security.oauth2.jwt.JwtGenerator;
import com.iba.security.oauth2.jwt.JwtUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: 11.06.2019 Refactoring by application-test.prop
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("JUnit_username")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LangControllerTest {

    private static String accessToken;
    private static String refreshToken;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setTokens() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assert user != null;
        Assert.assertEquals(user.getUsername(), "JUnit_username");
        accessToken = "Token " + jwtGenerator.generateAccess(new JwtUser(user.getId(), user.getUsername(), new Date().getTime()));
        refreshToken = "Refresh " + user.getRefreshToken();
    }

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private LangRepository langRepository;


    @Test
    public void getAllLangs() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/lang")
                // TODO: 09.06.2019 Check this after install oath2
                //.with(user(TEST_USER_ID))
                //.with(csrf())
                .header("Auth", accessToken)
                .header("AuthRef", refreshToken)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<Lang> listLangs = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<List<Lang>>() {
        });
        List<Lang> findByRep = langRepository.findAll();
        assert listLangs.size() == findByRep.size();
    }

    @Test
    public void getFreeLangsFailed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/lang")
                .header("Auth", accessToken)
                .header("AuthRef", refreshToken)
                .accept(MediaType.ALL)
                .content(String.valueOf(new Project((long) -1).getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getFreeLagsSuccess() throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Project> projects = projectRepository.findByAuthor(user);
        assert projects.size() != 0;

        Project project = projects.get(0);
        Assert.assertNotNull(project);

        List<Lang> langs = new ArrayList<>();
        for (ProjectLang projectLang : project.getProjectLangs()) {
            langs.add(projectLang.getLang());
        }

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/lang")
                .header("Auth", accessToken)
                .header("AuthRef", refreshToken)
                .accept(MediaType.ALL)
                .content(String.valueOf(project.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<Lang> listLangs = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<List<Lang>>() {
        });
        Assert.assertFalse(listLangs.containsAll(langs));
    }
}


*/
