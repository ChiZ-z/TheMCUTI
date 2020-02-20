package com.iba.repository;

import com.iba.model.project.ProjectContributor;
import com.iba.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectContributorRepository extends JpaRepository<ProjectContributor, Long> {
    /**
     * Find ProjectContributor
     * by project_contributor.id
     *
     * @param id id of ProjectContributor
     * @return ProjectContributor
     */
    @Query("select projectContributor from ProjectContributor projectContributor where projectContributor.id = :projectContributorId and projectContributor.isDeleted = false ")
    ProjectContributor findById(long projectContributorId);

    /**
     * Find list of ProjectContributors id
     * by project_contributor.user_id
     *
     * @param user id of ProjectContributor
     * @return list of ProjectContributors id
     */
    @Query("select projectContributor.projectId from ProjectContributor projectContributor where projectContributor.contributor = :user and projectContributor.isDeleted = false ")
    List<Long> findByContributor(User user);

    /**
     * Count all contributors
     * by project_contributor.project_id.
     *
     * @param projectId id of project
     * @return count of all contributors
     */
    @Query("select count(projectContributor.id) from ProjectContributor projectContributor where projectContributor.projectId = :projectId and projectContributor.isDeleted = false ")
    long countAllByProjectId(long projectId);

    @Modifying
    @Query("update ProjectContributor contributor set contributor.isDeleted = true where contributor.id = :id")
    @Override
    void deleteById(Long id);
}
