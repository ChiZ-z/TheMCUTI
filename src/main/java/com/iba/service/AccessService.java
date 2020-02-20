package com.iba.service;

import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.glossary.Glossary;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.ProjectRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(AccessService.class);

    private final ProjectRepository projectRepository;

    @Autowired
    public AccessService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Check access to a project.
     *
     * @param project   - project
     * @param user      - authenticated user
     * @param checkRole - check role if project is shared
     * @return true if access allowed, false if access denied
     */
    private boolean projectAccessDenied(Project project, User user, boolean checkRole) {
        if (project.getAuthor().getId().equals(user.getId())) {
            return false;
        }
        if (!checkRole) {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId().equals(user.getId()))) {
                return false;
            }
        } else {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId().equals(user.getId()) && a.getRole().name().equals("MODERATOR"))) {
                return false;
            }
        }
        logger.error("Access denied, User " + user.getUsername() + " isn't an author or contributor to project " + project.getProjectName());
        return true;
    }

    private boolean glossaryAuthorOrModeratorAccessDeniedWithCheckCLose(Glossary glossary, User user, boolean checkRole) {
        switch (glossary.getGlossaryType()) {
            case PUBLIC: {
                if (checkRole) {
                    if (glossary.getAuthor().equals(user)) {
                        return false;
                    }
                    return glossary.getFollowers().stream().noneMatch(follower -> follower.getRole().equals(Constants.FollowerRole.MODERATOR));
                } else {
                    return false;
                }
            }
            case PRIVATE: {
                if (glossary.getAuthor().equals(user)) {
                    return false;
                }
                return glossary.getFollowers().stream().noneMatch(follower -> follower.getRole().equals(Constants.FollowerRole.MODERATOR));
            }
        }
        return true;

    }

    /**
     * Check access to a project if exist.
     *
     * @param project   - project
     * @param user      - authenticated user
     * @param checkRole - check role if project is shared
     * @throws Exception_404 - if project not found.
     * @throws Exception_403 - if access denied.
     */
    public void isNotProjectOrAccessDenied(Project project, User user, boolean checkRole) {
        isNotObject(project);
        if (project.isDeleted()) {
            logger.error("Object not found");
            throw new Exception_404("Object not found!");
        }
        if (projectAccessDenied(project, user, checkRole)) {
            logger.debug("Access denied!");
            throw new Exception_403("Access denied!");
        }
    }

    /**
     * Return enum of user role in project.
     *
     * @param project - project
     * @param user    - authenticated user
     * @return enum of user role
     */
    public Constants.ContributorRole getUserRole(Project project, User user) {
        if (project.getAuthor().getId().equals(user.getId())) {
            return Constants.ContributorRole.AUTHOR;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId().equals(user.getId()) && a.getRole().name().equals("MODERATOR"))) {
            return Constants.ContributorRole.MODERATOR;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId().equals(user.getId()))) {
            return Constants.ContributorRole.TRANSLATOR;
        }
        logger.debug("Access denied!");
        throw new Exception_403("Access denied!");
    }

    /**
     * Checking is user author of project
     *
     * @param project - project
     * @param user    - @param user - authenticated user
     * @throws Exception_403 if access is denied!
     */
    public void isNotProjectOrNotAuthor(Project project, User user) {
        isNotObject(project);
        if (project.isDeleted()) {
            logger.error("Object not found");
            throw new Exception_404("Object not found!");
        }
        if (!project.getAuthor().getId().equals(user.getId())) {
            logger.error("User " + user.getUsername() + " isn't an author to project " + project.getProjectName());
            throw new Exception_403("Access denied!");
        }
    }

    /**
     * Verification of the object
     *
     * @param object - incoming object
     * @throws Exception_404 if term not found
     */
    public <T> void isNotObject(T object) {
        if (object == null) {
            logger.error("Object not found");
            throw new Exception_404("Object not found!");
        }
    }

    /**
     * Check access to a projectLang if exist.
     *
     * @param projectLang - projectLang
     * @param user        - authenticated user
     * @param checkRole   - check role if projectLang is shared
     * @throws Exception_404 - if projectLang not found.
     * @throws Exception_403 - if access denied.
     */
    public void isNotProjectLangOrAccessDenied(ProjectLang projectLang, User user, boolean checkRole) {
        isNotObject(projectLang);
        if (projectRepository.existsByProjectIdAndIsDeleted(projectLang.getProjectId())) {
            logger.error("Object not found");
            throw new Exception_404("Object not found!");
        }
        if (projectAccessDenied(projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId()), user, checkRole)) {
            logger.debug("Access denied!");
            throw new Exception_403("Access denied!");
        }
    }


    public void glossaryAccessDenied(Glossary glossary, User user,boolean checkRole) {
        isNotObject(glossary);
        if (glossaryAuthorOrModeratorAccessDeniedWithCheckCLose(glossary, user,checkRole)) {
            throw new Exception_403("Access denied!");
        }
    }
}
