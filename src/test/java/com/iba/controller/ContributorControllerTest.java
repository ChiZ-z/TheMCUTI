/*
package com.iba.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iba.model.chart.ResultStat;
import com.iba.model.project.ProjectContributor;
import com.iba.model.user.UserLang;
import com.iba.model.view.Constants;
import com.iba.repository.ProjectContributorRepository;
import com.iba.repository.ProjectRepository;
import com.iba.repository.UserRepository;
import com.iba.security.oauth2.jwt.JwtGenerator;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource("classpath:/application-test.properties")
@ContextConfiguration
public class ContributorControllerTest {

    private static final String URL = "/contributors";

    @Value("${access.token.JUnit_contributor}")
    private String accessTokenJUnit_contributor;

    @Value("${refresh.token.JUnit_contributor}")
    private String refreshTokenJUnit_contributor;

    @Value("${access.token.JUnit_username}")
    private String accessTokenJUnit_username;

    @Value("${refresh.token.JUnit_username}")
    private String refreshTokenJUnit_username;

    @Value("${project.contributor.translator.JUnit_username}")
    private String translator;

    @Value("${project.contributor.moderator.JUnit_username}")
    private String moderator;

    @Autowired
    private MockMvc mockMvc;

    @Resource
    private ProjectContributorRepository contributorRepository;

    @Test
    public void updateContributor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/update")
                .header("Auth", accessTokenJUnit_contributor)
                .header("AuthRef", refreshTokenJUnit_contributor)
                .param("id", translator)
                .content(String.valueOf(Constants.ContributorRole.TRANSLATOR))
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
    }

    @Test
    public void updateContributor403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/update")
                .header("Auth", accessTokenJUnit_username)
                .header("AuthRef", refreshTokenJUnit_username)
                .param("id", translator)
                .content(String.valueOf(Constants.ContributorRole.TRANSLATOR))
                .accept(MediaType.ALL))
                .andExpect(status().is(403));
    }

    @Test
    public void updateContributor404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/update")
                .header("Auth", accessTokenJUnit_contributor)
                .header("AuthRef", refreshTokenJUnit_contributor)
                .param("id", "-1")
                .content(String.valueOf(Constants.ContributorRole.TRANSLATOR))
                .accept(MediaType.ALL))
                .andExpect(status().is(404));
    }

    @Test
    public void getContributorStats() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}/stats", translator)
                .header("Auth", accessTokenJUnit_contributor)
                .header("AuthRef", refreshTokenJUnit_contributor)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        ResultStat resultStat = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<ResultStat>() {
        });
        Assert.assertNotNull(resultStat);
    }

    // TODO: 12.06.2019 When add new users for auth
    */
/*@Test
    public void getContributorStats403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/{id}/stats", translator)
                .header("Auth", accessTokenJUnit_contributor)
                .header("AuthRef", refreshTokenJUnit_contributor)
                .accept(MediaType.ALL))
                .andExpect(status().is(403));
    }*//*


    @Test
    public void getContributorStats404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}/stats", "-1")
                .header("Auth", accessTokenJUnit_contributor)
                .header("AuthRef", refreshTokenJUnit_contributor)
                .accept(MediaType.ALL))
                .andExpect(status().is(404));
    }
}
*/
