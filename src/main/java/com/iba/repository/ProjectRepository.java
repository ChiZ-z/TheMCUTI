package com.iba.repository;

import com.iba.model.project.Project;
import com.iba.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select project from Project project where project.id = " +
            "(select projectLang.projectId from ProjectLang projectLang where projectLang.id = " +
            "(select termLang.projectLangId from TermLang termLang where termLang.id = :termLangId))")
    Project findByTermLangId(Long termLangId);

    @Query("select project from Project project where project.id = " +
            "(select term.projectId from Term term where term.id = :termId)")
    Project findByTermId(Long termId);

    @Query("select project from Project project where project.id = " +
            "(select projectLang.projectId from ProjectLang projectLang where projectLang.id = :projectLangId)")
    Project findByProjectLangId(Long projectLangId);

    @Query("select case " +
            "when project.isDeleted = true then true " +
            "when count(project.id) = 0 then true " +
            "else false end from Project project where project.id = :projectId")
    boolean existsByProjectIdAndIsDeleted(Long projectId);

    /**
     * Find list of user projects
     * by project.author_id.
     *
     * @param author - user
     * @return list of user projects
     */
    @Query("select project from Project project where project.author = :author and project.isDeleted = false ")
    List<Project> findByAuthorAndIsDeletedFalse(User author);

    /**
     * Find project
     * by project.id.
     *
     * @param id - id of project
     * @return project
     */
    @Transactional
    @Query("select project from Project project where project.id = :projectId and project.isDeleted = false ")
    Project findByIdAndIsDeletedFalse(long projectId);

    @Transactional
    @Query("select project from Project project where project.id = :projectId and project.isDeleted = false ")
    Project findById(long projectId);

    /**
     * Find list of projects
     * by project.author_id == contributor..id.
     *
     * @param contributors - list of contributors
     * @return list of projects
     */
    @Transactional
    @Query("select project from Project project where id in ?1 and project.isDeleted = false ")
    List<Project> findByContributorsAndIsDeletedFalse(List<Long> contributors);

    /**
     * Find list of projects
     * by project.author_id == contributor..id
     * and project.author_id == author.id.
     *
     * @param contributors - list of contributors
     * @param user         - author
     * @return list of projects
     */
    @Transactional
    @Query("select project from Project project where project.author = :user and project.isDeleted = false or id in :contributors ")
    List<Project> findByAuthorAndContributorsAndIsDeletedFalse(User user, List<Long> contributors);

    /**
     * Find projects
     * by project.project_name && project.author.
     *
     * @param author - author
     * @param name   - projectName
     * @return number of found projects
     */
    // TODO: 17.07.2019 change to boolean
    @Query("select count(project) from Project project where project.author = :author and project.projectName = :projectName and project.isDeleted = false ")
    int countByAuthorAndProjectNameAndIsDeletedFalse(User author, String projectName);

    /**
     * Find projects
     * by project.author.
     *
     * @param user - author
     * @return number of found projects
     */
    @Query("select count(project) from Project project where project.author = :user and project.isDeleted = false ")
    Long countAllByAuthorAndIsDeletedFalse(User user);

    /**
     * Get author id
     * by project.author_id.
     *
     * @param id - id of project
     * @return id of author
     */
    @Query("select project.author.id from Project  project where project.id = :projectId and project.isDeleted = false")
    Long getAuthorIdByProjectIdAndIsDeletedFalse(Long projectId);

    /**
     * Count all user projects
     * by project.author_id == userId && project_contributor.id == userId.
     *
     * @param userId - id of user
     * @return number of user projects
     */
    @Query("select count(project) from Project project where project.author.id = :userId and project.isDeleted = false " +
            "or project.id in (select projectContributor.projectId from ProjectContributor projectContributor where projectContributor.contributor.id = :userId)")
    Long countAllUserProjectsAndIsDeletedFalse(Long userId);

    @Modifying
    @Query("update Project project set project.isDeleted = true where project = :project")
    @Override
    void delete(Project project);
}
