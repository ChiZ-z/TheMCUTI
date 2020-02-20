package com.iba.repository;

import com.iba.model.project.Lang;
import com.iba.model.project.ProjectLang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectLangRepository extends JpaRepository<ProjectLang, Long> {

    @Query("select case " +
            "when projectLang.isDeleted = true then true " +
            "when count(projectLang.id) = 0 then true " +
            "when (select project.isDeleted from Project project where project.id = projectLang.projectId) = true then true " +
            "when (select count(project.id) from Project project where project.id = projectLang.projectId) = 0 then true " +
            "else false end from ProjectLang projectLang where projectLang.id = :projectLangId")
    boolean existsByProjectLangIsDeletedAndProjectIsDeleted(long projectLangId);

    /**
     * Find project_lang
     * by project_lang.id
     *
     * @param id - id of projectlang
     * @return ProjectLang
     */
    @Query("select projectLang from ProjectLang projectLang where projectLang.id = :projectLangId and projectLang.isDeleted = false ")
    ProjectLang findById(long projectLangId);

    /**
     * Find default ProjectLang
     * by project_lang.project_id && project_lang.is_default = true.
     *
     * @param projectId - id of project
     * @return default ProjectLang
     */
    @Query(value = "select projectLang from ProjectLang projectLang where projectLang.projectId = :projectId and projectLang.isDefault = true and projectLang.isDeleted = false ")
    ProjectLang findByDefaultAndProjectId(long projectId);

    /**
     * Select all lang in project
     * by project_lang.project_id
     *
     * @param projectId - id of project
     * @return list of Langs that ProjectLang contains
     */
    @Query(value = "select projectLang.lang from ProjectLang projectLang where projectLang.projectId = :projectId and projectLang.isDeleted = false ")
    List<Lang> findListOfLangs(long projectId);

    @Modifying
    @Query("update ProjectLang projLang set projLang.isDeleted = true where projLang = :projectLang")
    @Override
    void delete(ProjectLang projectLang);
}
