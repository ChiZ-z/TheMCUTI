package com.iba.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.exceptions.Exception_400;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.exceptions.Exception_424;
import com.iba.model.project.Project;
import com.iba.model.project.Term;
import com.iba.model.project.TermComment;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.View;
import com.iba.repository.ProjectRepository;
import com.iba.repository.TermCommentRepository;
import com.iba.repository.TermLangRepository;
import com.iba.repository.TermRepository;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import com.iba.service.ValidatorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/terms")
public class TermController {

    private final TermRepository termRepository;

    private final TermLangRepository termLangRepository;

    private final ProjectRepository projectRepository;

    private final AccessService accessService;

    private final TermCommentRepository termCommentRepository;

    private final HistoryService historyService;

    private final ValidatorService validatorService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(TermController.class);

    @Autowired
    public TermController(TermRepository termRepository, TermLangRepository termLangRepository, ProjectRepository projectRepository,
                          AccessService accessService, TermCommentRepository termCommentRepository, HistoryService historyService, ValidatorService validatorService) {
        this.termRepository = termRepository;
        this.termLangRepository = termLangRepository;
        this.projectRepository = projectRepository;
        this.accessService = accessService;
        this.termCommentRepository = termCommentRepository;
        this.historyService = historyService;
        this.validatorService = validatorService;
    }

    /**
     * Get TermLangs by Term.
     *
     * @param term - Term
     * @param user - authenticated User
     * @return list of TermLangs by Term
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Term or Project not found
     */
    @GetMapping("/{termId}/translations")
    @JsonView(View.ProjectItem.class)
    public List<TermLang> getTranslations(@PathVariable("termId") Term term, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to get project term translations");
        accessService.isNotObject(term);
        Project project = projectRepository.findByIdAndIsDeletedFalse(term.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        logger.debug("User " + user.getUsername() + " got project terms");
        return termLangRepository.findByTerm(term).stream().sorted(Comparator.comparing(a -> a.getLang().getId())).collect(Collectors.toList());
    }

    /**
     * Update value of Term in Project.
     *
     * @param term     - exist Term
     * @param newValue - new value of Term
     * @param user     - authenticated User
     * @throws Exception_400 if Term exists in Project
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Term or Project not found
     * @throws Exception_424 if validation of the Term is failed
     */
    @PutMapping("/{termId}/update")
    public void updateTerm(@PathVariable("termId") Term term, @RequestBody String newValue,
                           @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to update project term");
        accessService.isNotObject(term);
        Project project = projectRepository.findByIdAndIsDeletedFalse(term.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, true);
        if (termRepository.existsByProjectIdAndTermValue(project.getId(), newValue)) {
            throw new Exception_400("Term is exist");
        }
        if (!validatorService.validateTermValue(newValue) || term.getTermValue().equals(newValue)) {
            throw new Exception_424("Validate term value failed!");
        }
        String oldValue = term.getTermValue();
        term.setTermValue(newValue);
        termRepository.save(term);
        historyService.createTermEvent(Constants.StatType.EDIT_TERM, user, project, term, oldValue, newValue);
        logger.debug("User " + user.getUsername() + " updated project terms");
    }

    /**
     * Get comments from Term.
     *
     * @param term - id of Term
     * @param user - authenticated User
     * @return list of comments
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Term or Project not found
     */
    @GetMapping("/{termId}/get-comments")
    @JsonView(View.ProjectItem.class)
    public List<TermComment> getComments(@PathVariable("termId") Term term, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to get project term comments");
        accessService.isNotObject(term);
        Project project = projectRepository.findByIdAndIsDeletedFalse(term.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return termCommentRepository.findAllByTermId(term.getId()).stream().sorted(Comparator.comparing(TermComment::getCreationDate).reversed()).collect(Collectors.toList());
    }

    /**
     * Add new comment to Term.
     *
     * @param term - id of Term
     * @param user - authenticated User
     * @param text - text of new comment
     * @return new comment
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Term or Project not found
     */
    @PostMapping("/{termId}/add-comment")
    @JsonView(View.ProjectItem.class)
    public TermComment addComment(@NotNull @PathVariable("termId") Term term, @AuthenticationPrincipal User user, @RequestBody String text) {
        logger.debug("User " + user.getUsername() + " is trying to add project term comment");
        accessService.isNotObject(term);
        Project project = projectRepository.findByIdAndIsDeletedFalse(term.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        TermComment termComment = new TermComment(term.getId(), user, text);
        termCommentRepository.save(termComment);
        return termComment;
    }

    /**
     * Delete comment.
     *
     * @param comment - comment for delete
     * @param user    - authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Term or comment not found
     */
    @PostMapping("/delete-comment")
    public void deleteComment(@RequestBody TermComment comment, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete project term comment");
        accessService.isNotObject(comment);
        Term term = termRepository.findById((long) comment.getTermId());
        accessService.isNotObject(term);
        long authId = projectRepository.getAuthorIdByProjectIdAndIsDeletedFalse(term.getProjectId());
        if (authId != user.getId() && !comment.getAuthor().equals(user)) {
            throw new Exception_403("Access denied.");
        }
        termCommentRepository.delete(comment);
    }

}
