package com.iba.controller;

import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.exceptions.Exception_424;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.repository.ProjectLangRepository;
import com.iba.repository.ProjectRepository;
import com.iba.repository.TermLangRepository;
import com.iba.service.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/term-lang")
public class TermLangController {

    private final AccessService accessService;

    private final TermLangRepository termLangRepository;

    private final ProjectLangRepository projectLangRepository;

    private final ProjectRepository projectRepository;

    private final BitFlagService bitFlagService;

    private final ProjectLangService projectLangService;

    private final HistoryService historyService;

    private final ValidatorService validatorService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(TermLangController.class);

    @Autowired
    public TermLangController(TermLangRepository termLangRepository, ProjectLangRepository projectLangRepository, ProjectRepository projectRepository,
                              BitFlagService bitFlagService, ProjectLangService projectLangService, HistoryService historyService, AccessService accessService, ValidatorService validatorService) {
        this.termLangRepository = termLangRepository;
        this.projectLangRepository = projectLangRepository;
        this.projectRepository = projectRepository;
        this.bitFlagService = bitFlagService;
        this.projectLangService = projectLangService;
        this.historyService = historyService;
        this.accessService = accessService;
        this.validatorService = validatorService;
    }

    /**
     * Update value of TermLang.
     *
     * @param termLang - exist TermLang
     * @param newVal   - new value of TermLang
     * @param user     - authenticated User
     * @return updated TermLang
     * @throws Exception_403 if access denied
     * @throws Exception_404 if TermLang or Project not found
     * @throws Exception_424 if validate of TermLang failed
     */
    @PutMapping("/{termLangId}/update")
    public TermLang updateTermLang(@PathVariable("termLangId") TermLang termLang,
                                   @RequestBody(required = false) String newVal,
                                   @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to update term lang value");
        Project project = projectRepository.findById((long) termLang.getTerm().getProjectId());
        accessService.isNotObject(termLang);
        accessService.isNotProjectOrAccessDenied(project, user, false);
        if (newVal == null) {
            logger.error("New value is null");
            bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
            newVal = "";
        }
        if (!validatorService.validateTermLangValue(newVal)) {
            throw new Exception_424("Limit length!");
        }
        bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.AUTOTRANSLATED);
        String oldValue = termLang.getValue();
        if (!oldValue.equals(newVal)) {
            termLang.setValue(newVal);
            termLang.setModifier(user);
            termLang.setModifiedDate();
            ProjectLang projectLang = projectLangRepository.findById(termLang.getProjectLangId());
            if (projectLang.isDefault()) {
                List<TermLang> termLangs = termLangRepository.findByTerm(termLang.getTerm());
                termLangs.remove(termLang);
                termLangs.forEach(a -> {
                    if (!a.getValue().equals("")) {
                        bitFlagService.addFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                    }
                });
            } else {
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            }
            termLangRepository.save(termLang);
            projectLangService.setFlagsToTerm(termLang);
            historyService.simpleEdit(user, project, termLang, projectLang, oldValue, newVal);
            logger.debug("User " + user.getUsername() + " updated term lang value");
        }
        return termLang;
    }

    /**
     * Set TermLang status to fuzzy.
     *
     * @param termLang - incoming TermLang
     * @param user     - authenticated User
     * @param fuzzy    - flag fuzzy,
     *                 if false dropFlag,
     *                 if true addFlag
     * @throws Exception_403 if access denied
     * @throws Exception_404 if TermLang or Project not found
     */
    @PutMapping("/{termLangId}/fuzzy")
    public void fuzzy(@PathVariable("termLangId") TermLang termLang, @AuthenticationPrincipal User user,
                      @RequestParam Boolean fuzzy) {
        logger.debug("User " + user.getUsername() + " is trying to mark term lang like fuzzy");
        accessService.isNotObject(termLang);
        accessService.isNotProjectOrAccessDenied(projectRepository.findByIdAndIsDeletedFalse(termLang.getTerm().getProjectId()), user, false);
        if (fuzzy != null && fuzzy) {
            bitFlagService.addFlag(termLang, BitFlagService.StatusFlag.FUZZY);
        } else if (fuzzy != null) {
            bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
        }
        termLangRepository.save(termLang);
        logger.debug("User " + user.getUsername() + " marked term lang like fuzzy");
    }

    /**
     * Drop DEFAULT_WAS_CHANGED flag from TermLang.
     *
     * @param termLang TermLang
     * @return TermLang with dropped flag
     */
    @PutMapping("/{termLangId}/drop-flag")
    public TermLang dropDefaultFlagFromTermLang(@PathVariable("termLangId") TermLang termLang) {
        bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
        termLangRepository.save(termLang);
        return termLang;
    }
}
