package com.iba.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.history.History;
import com.iba.model.history.ResultHistory;
import com.iba.model.project.Project;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;
import com.iba.repository.HistoryRepository;
import com.iba.repository.ProjectRepository;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import com.iba.utils.PagesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/history")
@RestController
public class HistoryController {

    private final HistoryService historyService;

    private final HistoryRepository historyRepository;

    private final AccessService accessService;

    private final PagesUtil pagesUtil;

    private final ProjectRepository projectRepository;

    private static final List<Constants.StatType> constantStatTypeList = Arrays.asList(Constants.StatType.EDIT, Constants.StatType.TRANSLATE,
            Constants.StatType.AUTO_TRANSLATE, Constants.StatType.EDIT_BY_IMPORT, Constants.StatType.TRANSLATE_BY_IMPORT);

    @Autowired
    public HistoryController(HistoryService historyService, HistoryRepository historyRepository, AccessService accessService, PagesUtil pagesUtil, ProjectRepository projectRepository) {
        this.historyService = historyService;
        this.historyRepository = historyRepository;
        this.accessService = accessService;
        this.pagesUtil = pagesUtil;
        this.projectRepository = projectRepository;
    }

    /**
     * Get History of Project depending on incoming parameters.
     *
     * @param project       Project
     * @param statTypes     actions of History
     * @param contributorId id of contributor for find
     * @param start         date start from (yyyy-MM-dd)
     * @param end           date end from (yyyy-MM-dd)
     * @param page          page
     * @param user          authenticated User
     * @return not entity class ResultHistory with page parameters and list of History actions of Project
     * @throws ParseException if parse data failed
     * @throws Exception_403  if access denied
     * @throws Exception_404  if Project not found
     */
    @JsonView(View.HistoryItem.class)
    @GetMapping("/{projectId}/filter")
    public ResultHistory getProjectHistory(@PathVariable("projectId") Project project,
                                           @RequestParam List<Constants.StatType> statTypes,
                                           @RequestParam(required = false) Long contributorId,
                                           @RequestParam(required = false) String start,
                                           @RequestParam(required = false) String end,
                                           Pageable page,
                                           @AuthenticationPrincipal User user) throws ParseException {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        if (statTypes.isEmpty()) {
            return new ResultHistory(new PageParams(0, 0, 0), new ArrayList<>());
        }
        List<History> historyList = historyService.createHistoryList(project.getId(), contributorId, statTypes, start, end);
        PageParams pageParams = pagesUtil.createPagesParamsByList(historyList, page);
        return new ResultHistory(pageParams, historyService.setIsDisabled(pagesUtil.createSubListByPage(historyList, pageParams, page.getPageSize())));
    }

    /**
     * Get History about user depending on incoming parameters.
     *
     * @param statTypes actions of History
     * @param projectId id of Project
     * @param start     date start from (yyyy-MM-dd)
     * @param end       date end from (yyyy-MM-dd)
     * @param page      page
     * @param user      authenticated User
     * @return not entity class ResultHistory with page parameters and list of History actions of user
     * @throws ParseException if parse data failed
     * @throws Exception_403  if access denied
     * @throws Exception_404  if Project not found
     */
    @JsonView(View.HistoryItem.class)
    @GetMapping("/filter")
    public ResultHistory getUserHistory(@RequestParam List<Constants.StatType> statTypes,
                                        @RequestParam(required = false) Long projectId,
                                        @RequestParam(required = false) String start,
                                        @RequestParam(required = false) String end,
                                        Pageable page,
                                        @AuthenticationPrincipal User user) throws ParseException {
        if (statTypes.isEmpty()) {
            return new ResultHistory(new PageParams(0, 0, 0), new ArrayList<>());
        }
        if (projectId != null) {
            Project project = projectRepository.findByIdAndIsDeletedFalse(projectId);
            accessService.isNotProjectOrAccessDenied(project, user, false);
        }
        List<History> historyList = historyService.createHistoryList(projectId, user.getId(), statTypes, start, end);
        PageParams pageParams = pagesUtil.createPagesParamsByList(historyList, page);
        return new ResultHistory(pageParams, historyService.setIsDisabled(pagesUtil.createSubListByPage(historyList, pageParams, page.getPageSize())));
    }

    /**
     * Get Histories by parent id of History.
     *
     * @param project Project
     * @param history History for find other Histories by parent id
     * @param user    authenticated User
     * @return list of History actions
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or History not found
     */
    @JsonView(View.HistoryItem.class)
    @GetMapping("/{projectId}/event/{historyId}")
    public List<History> getHistory(@PathVariable("projectId") Project project, @PathVariable("historyId") History history, @AuthenticationPrincipal User user) {
        accessService.isNotObject(history);
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return historyRepository.findAllByProjectIdAndParentId(project.getId(), history.getId(), constantStatTypeList);
    }

    /**
     * Get History by TermLang.
     *
     * @param project  Project
     * @param termLang TermLang
     * @param user     authenticated User
     * @return list of History actions
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or TermLang not found
     */
    @JsonView(View.HistoryItem.class)
    @GetMapping("/{projectId}/translation/{termLangId}")
    public List<History> getHistoryByTermLang(@PathVariable("projectId") Project project, @PathVariable("termLangId") TermLang termLang, @AuthenticationPrincipal User user) {
        accessService.isNotObject(termLang);
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return historyRepository.findAllByProjectIdAndTermLangId(project.getId(), termLang.getId(), constantStatTypeList);
    }
}
