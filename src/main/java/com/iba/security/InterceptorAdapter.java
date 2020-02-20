package com.iba.security;

import com.iba.exceptions.Exception_404;
import com.iba.model.project.Project;
import com.iba.model.user.User;
import com.iba.repository.*;
import com.iba.service.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class InterceptorAdapter extends HandlerInterceptorAdapter {

    private static int count = 0;

    private final AccessService accessService;

    private final ProjectRepository projectRepository;

    private final ProjectLangRepository projectLangRepository;

    private final TermRepository termRepository;

    private final TermLangRepository termLangRepository;

    private final GlossaryRepository glossaryRepository;

    private final CategoryRepository categoryRepository;

    private final GroupItemRepository groupItemRepository;

    private final TranslationItemRepository translationItemRepository;

    @Autowired
    public InterceptorAdapter(AccessService accessService, ProjectRepository projectRepository, ProjectLangRepository projectLangRepository, TermRepository termRepository, TermLangRepository termLangRepository, GlossaryRepository glossaryRepository, CategoryRepository categoryRepository, GroupItemRepository groupItemRepository, TranslationItemRepository translationItemRepository) {
        this.accessService = accessService;
        this.projectRepository = projectRepository;
        this.projectLangRepository = projectLangRepository;
        this.termRepository = termRepository;
        this.termLangRepository = termLangRepository;
        this.glossaryRepository = glossaryRepository;
        this.categoryRepository = categoryRepository;
        this.groupItemRepository = groupItemRepository;
        this.translationItemRepository = translationItemRepository;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        // TODO: 05.07.2019 User with username = anonymousUser

        if (pathVariables.get("projectId") != null) {
            accessToProject(projectRepository.findById(Long.parseLong(String.valueOf(pathVariables.get("projectId")))));
        }
        if (pathVariables.get("projectLangId") != null) {
            if (projectLangRepository.existsByProjectLangIsDeletedAndProjectIsDeleted(Long.parseLong(String.valueOf(pathVariables.get("projectLangId"))))) {
                throw new Exception_404("Object not found!");
            }
            accessToProject(projectRepository.findByProjectLangId(Long.parseLong(String.valueOf(pathVariables.get("projectLangId")))));
        }
        if (pathVariables.get("termId") != null) {
            if (termRepository.existsByTermIsDeletedAndProjectIsDeleted(Long.parseLong(String.valueOf(pathVariables.get("termId"))))) {
                throw new Exception_404("Object not found!");
            }
            accessToProject(projectRepository.findByTermId(Long.parseLong(String.valueOf(pathVariables.get("termId")))));
        }
        if (pathVariables.get("termLangId") != null) {
            if (termLangRepository.existsByTermIsDeletedAndProjectLangIsDeletedAndProjectIsDeleted(Long.parseLong(String.valueOf(pathVariables.get("termLangId"))))) {
                throw new Exception_404("Object not found!");
            }
            accessToProject(projectRepository.findByTermLangId(Long.parseLong(String.valueOf(pathVariables.get("termLangId")))));
        }
        if (pathVariables.get("glossaryId") != null) {
            if (!glossaryRepository.existsById(Long.parseLong(String.valueOf(pathVariables.get("glossaryId"))))) {
                throw new Exception_404("Object not found!");
            }
        }
        if (pathVariables.get("categoryId") != null) {
            if (!categoryRepository.existsById(Long.parseLong(String.valueOf(pathVariables.get("categoryId"))))) {
                throw new Exception_404("Object not found!");
            }
        }
        if (pathVariables.get("groupItemId") != null) {
            if (!groupItemRepository.existsById(Long.parseLong(String.valueOf(pathVariables.get("groupItemId"))))) {
                throw new Exception_404("Object not found!");
            }
        }
        if (pathVariables.get("translationItemId") != null) {
            if (!translationItemRepository.existsById(Long.parseLong(String.valueOf(pathVariables.get("translationItemId"))))) {
                throw new Exception_404("Object not found!");
            }
        }
        return true;
    }

    private void accessToProject(Project project) {
        if (project != null &&
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities().containsAll(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ANONYMOUS"))) {
            accessService.isNotProjectOrAccessDenied(project, (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), false);
        }
    }

    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
    }

    private static synchronized void incrementCount() {
        count++;
    }

    private static synchronized void decrementCount() {
        count--;
    }
}
