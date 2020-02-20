package com.iba.controller;

import com.iba.model.project.Lang;
import com.iba.model.project.Project;
import com.iba.model.user.User;
import com.iba.repository.LangRepository;
import com.iba.repository.ProjectLangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lang")
public class LangController {

    private final LangRepository langRepository;

    private final ProjectLangRepository projectLangRepository;

    @Autowired
    public LangController(LangRepository langRepository, ProjectLangRepository projectLangRepository) {
        this.langRepository = langRepository;
        this.projectLangRepository = projectLangRepository;
    }

    /**
     * Get all languages.
     *
     * @return list of all Languages
     */
    @GetMapping
    public List<Lang> getAll() {
        return langRepository.findAll();
    }

    /**
     * Get languages that are not use in Project
     *
     * @param project - Project
     * @return list of free languages
     */
    @PostMapping("/{projectId}")
    public List<Lang> getFreeLangs(@PathVariable("projectId") Project project) {
        List<Lang> langs = projectLangRepository.findListOfLangs(project.getId());
        return langRepository.findAll().stream().filter(a -> !langs.contains(a)).collect(Collectors.toList());
    }

    /**
     * Get User languages that User don't add
     *
     * @param user - authenticated User
     * @return list of free User languages
     */
    @GetMapping("user-langs")
    public List<Lang> getFreeUserLangs(@AuthenticationPrincipal User user) {
        List<Lang> userLangs = langRepository.findByUserId(user.getId());
        return langRepository.findAll().stream().filter(a -> !userLangs.contains(a)).collect(Collectors.toList());
    }
}
