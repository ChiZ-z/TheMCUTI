package com.iba.controller;

import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.chart.ResultStat;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectContributor;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.ProjectContributorRepository;
import com.iba.repository.ProjectRepository;
import com.iba.repository.UserRepository;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contributors")
public class ContributorController {

    private final ProjectContributorRepository contributorRepository;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final HistoryService historyService;

    private final AccessService accessService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ContributorController.class);

    @Autowired
    public ContributorController(ProjectContributorRepository contributorRepository, ProjectRepository projectRepository, UserRepository userRepository, HistoryService historyService, AccessService accessService) {
        this.contributorRepository = contributorRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
        this.accessService = accessService;
    }

    /**
     * Update role of Contributor in Project.
     *
     * @param projectContributor Contributor of Project
     * @param role               new role of Contributor
     * @param user               authenticated User
     * @throws Exception_404 if contributor(User) or Project not found
     * @throws Exception_403 if access denied
     */
    @PutMapping("/{projectContributorId}/update")
    public void updateContributor(@PathVariable("projectContributorId") ProjectContributor projectContributor, @RequestBody String role, @AuthenticationPrincipal User user) {
        if (projectContributor == null) {
            throw new Exception_404("Contributor not found.");
        }
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectContributor.getProjectId());
        accessService.isNotObject(project);
        if (!project.getAuthor().getId().equals(user.getId())) {
            logger.debug("Access denied!");
            throw new Exception_403("Access denied.");
        }
        projectContributor.setRole(Constants.ContributorRole.valueOf(role));
        contributorRepository.save(projectContributor);
    }

    /**
     * Get statistic of Contributor in Project.
     *
     * @param contributor Contributor
     * @param project     Project
     * @param user        authenticated User
     * @return not entity class ResultStat with stats of Project
     * @throws Exception_404 if contributor(User) or Project not found
     * @throws Exception_403 if access denied
     */
    @GetMapping("/{userId}/project/{projectId}/stats")
    public ResultStat getContributorStats(@PathVariable("userId") User contributor, @PathVariable("projectId") Project project,
                                          @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " try to get statistic of contributor");
        accessService.isNotObject(contributor);
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return historyService.getAllUserStatsInProject(contributor, project.getId());
    }

    /**
     * Get contributors of Project without author of this Project.
     *
     * @param project Project
     * @param user    authenticated User
     * @return Contributors of Project without author
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/all/{projectId}")
    public List<User> getContributors(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        List<User> resultUserList = new ArrayList<>(userRepository.findAllByProjectId(project.getId()));
        resultUserList.add(project.getAuthor());
        return resultUserList;
    }


}
