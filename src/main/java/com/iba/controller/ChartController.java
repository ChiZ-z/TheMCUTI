package com.iba.controller;

import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.exceptions.Exception_423;
import com.iba.model.chart.ChartItem;
import com.iba.model.project.Project;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.ProjectRepository;
import com.iba.repository.UserRepository;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RequestMapping("/statistic")
@RestController
public class ChartController {

    private final HistoryService historyService;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final AccessService accessService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ChartController.class);

    @Autowired
    public ChartController(HistoryService historyService, ProjectRepository projectRepository, UserRepository userRepository, AccessService accessService) {
        this.historyService = historyService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
    }

    /**
     * Get statistics by period (start - end),
     * by projectId if param exists,
     * by contributorId if param exists.
     *
     * @param user          authenticated User
     * @param projectId     id of Project (required = false)
     * @param contributorId id of Contributor in Project (required = false)
     * @param start         date start from (yyyy-MM-dd)
     * @param end           date end from (yyyy-MM-dd)
     * @param type          type of statistic
     * @return not entity class ChartItem with statistic by period
     * @throws ParseException if parse date failed
     * @throws Exception_403  if access denied
     * @throws Exception_404  if Project not found
     * @throws Exception_423  if incorrect params
     */
    @GetMapping
    public ChartItem getStats(@AuthenticationPrincipal User user,
                              @RequestParam(required = false) Long projectId,
                              @RequestParam(required = false) Long contributorId,
                              @RequestParam String start,
                              @RequestParam String end,
                              @RequestParam Constants.StatType type) throws ParseException {

        logger.debug("User " + user.getUsername() + " get stats. Start: " + start + " End: " + end);
        if (projectId == null && contributorId == null) {
            throw new Exception_423("Incorrect params.");
        }
        if (projectId != null) {
            Project project = projectRepository.findByIdAndIsDeletedFalse(projectId);
            accessService.isNotProjectOrAccessDenied(project, user, false);
            if (contributorId != null) {
                accessService.isNotProjectOrAccessDenied(project, userRepository.findById((long) contributorId), false);
            }
        } else if (!contributorId.equals(user.getId())) {
            throw new Exception_403("Access denied.");
        }
        return historyService.createChartItem(projectId, contributorId, type, start, end);
    }
}
