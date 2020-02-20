package com.iba.repository;

import com.iba.model.project.Term;
import com.iba.model.project.TermLang;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface TermLangRepository extends JpaRepository<TermLang, Long> {

    @Query(value = "select case " +
            "when count(termLang.id) = 0 then true " +
            "when (select term.isDeleted from Term term where term.id = termLang.term.id) = true then true " +
            "when (select count(term.id) from Term term where term.id = termLang.term.id) = 0 then true " +
            "when (select projectLang.isDeleted from ProjectLang projectLang where projectLang.id = termLang.projectLangId) = true then true " +
            "when (select projectLang.id from ProjectLang projectLang where projectLang.id = termLang.projectLangId) = 0 then true " +
            "when (select project.isDeleted from Project project where project.id = " +
            "(select projectLang.projectId from ProjectLang projectLang where projectLang.id = termLang.projectLangId)) = true then true " +
            "when (select count(project.id) from Project project where project.id = " +
            "(select projectLang.projectId from ProjectLang projectLang where projectLang.id = termLang.projectLangId)) = 0 then true " +
            "else false end from TermLang termLang where termLang.id = :termLangId")
    boolean existsByTermIsDeletedAndProjectLangIsDeletedAndProjectIsDeleted(long termLangId);

    /**
     * Find List of TermLang
     * by term
     *
     * @param term - Term for find TermLangs
     * @return list of TermLangs
     */
    @Query("select termLang from TermLang termLang where termLang.term = :term and termLang.term.isDeleted = false and termLang.projectLangId in " +
            "(select projectLang from ProjectLang projectLang where projectLang.id = termLang.projectLangId and projectLang.isDeleted = false )")
    List<TermLang> findByTerm(Term term);

    /**
     * Count translated (value isn't empty) TermLangs
     * by term_lang.project_lang_id.
     *
     * @param projectLangId - id of projectLang
     * @return number of translated (value isn't empty) termLangs
     */
    @Query(value = "select count(termLang) from TermLang termLang where termLang.value<>'' and termLang.projectLangId in " +
            "(select projectLang from ProjectLang projectLang where projectLang.id = :projectLangId and projectLang.isDeleted = false) and " +
            "termLang.term in (select term from Term term where term.isDeleted = false )")
    long countAllByProjectLangIdAndValueIsNotEmpty(long projectLangId);

    /**
     * Find list of TermLangs
     * by ProjectLang && page.
     *
     * @param id       - id of ProjectLang
     * @param pageable - page
     * @return list of TermLangs by ProjectLang
     */
    List<TermLang> findByProjectLangId(long id, Pageable pageable);

    /**
     * Count number of translated termLangs
     * by term_lang.term_id && term_lang.value != ' '.
     *
     * @param termsId - id of term
     * @return Count of translated termLangs
     */
    @Query(value = "select sum(case when termLang.value <> '' then 1 else 0 end) from TermLang termLang where termLang.term in " +
            "(select term from Term term where term.id = :termId and term.isDeleted = false) and termLang.projectLangId in " +
            "(select projectLang.id from ProjectLang projectLang where projectLang.id = termLang.projectLangId and projectLang.isDeleted = false )")
    long countNotEmptyTranslationByTermsId(Long termId);

    /**
     * Count number of all termLangs
     * by term_lang.projectId && term_lang.value isn't null.
     *
     * @param projectId - id of project
     * @return Count of all termLangs
     */
    @Query(value = "select count(termLang) from TermLang termLang where termLang.value is not null and termLang.projectLangId in " +
            "(select projectLang.id from ProjectLang projectLang where projectLang.projectId = :projectId and projectLang.isDeleted = false " +
            "and termLang.term.id in (select term.id from Term term where termLang.term.id = term.id and term.isDeleted = false) )")
    long countAllTranslation(long projectId);

    /**
     * Count number of translated termLangs
     * by term_lang.projectId && term_lang.value != ' '.
     *
     * @param projectId - id of project
     * @return Count of translated termLangs
     */
    @Query(value = "select count(termLang) from TermLang termLang where termLang.value<>'' and termLang.projectLangId in " +
            "(select projectLang.   id from ProjectLang projectLang where projectLang.projectId = ?1 and projectLang.isDeleted = false " +
            "and termLang.term.id in (select term.id from Term term where termLang.term.id = term.id and term.isDeleted = false))")
    long countNotEmptyTranslation(long projectId);

    /**
     * Count list of numbers of all termLangs
     * by term_lang.user_id && term_lang.value isn't null.
     *
     * @param userId - id of user
     * @return Count of list of numbers of all termLangs
     */
    @Query(value = "select sum(case when termLang.value is not null then 1 else 1 end) from TermLang termLang " +
            "inner join ProjectLang projectLang on termLang.projectLangId = projectLang.id and projectLang.isDeleted = false " +
            "inner join Project project on projectLang.projectId = project.id and project.isDeleted = false and (project.author.id = :userId or project.id in " +
            "(select projectId from ProjectContributor projectContributor where projectContributor.contributor.id = :userId and projectContributor.isDeleted = false )) " +
            "where termLang.term.id in (select term.id from Term term where termLang.term.id = term.id and term.isDeleted = false) group by project.id")
    List<Long> countListAllTranslationByUserId(long userId);

    /**
     * Count list of numbers of translated termLangs
     * by term_lang.user_id && term_lang.value != ' '.
     *
     * @param userId - id of user
     * @return Count of list of numbers of translated termLangs
     */
    @Query(value = "select sum(case when termLang.value <> '' then 1 else 0 end) from TermLang termLang " +
            "inner join ProjectLang projectLang on termLang.projectLangId = projectLang.id and projectLang.isDeleted = false " +
            "inner join Project project on projectLang.projectId = project.id and project.isDeleted = false " +
            "where (project.author.id = :userId and project.isDeleted = false or project.id in " +
            "(select projectId from ProjectContributor projectContributor where projectContributor.contributor.id = :userId and projectContributor.isDeleted = false )) " +
            "and termLang.term.id in (select term.id from Term term where termLang.term.id = term.id and term.isDeleted = false) group by project.id")
    List<Long> countListNotEmptyTranslationByUserId(long userId);

    @Query(value = "select termLang from TermLang termLang where termLang.value<>'' and termLang.projectLangId = :projectLangId")
    List<TermLang> findAllByProjectLangIdAndValueIsNotEmpty(long projectLangId);

    @Transactional
    @Modifying
    @Query(value = "update TermLang t set t.status = 0 where t.projectLangId = :projectLangId")
    void resetStatusByProjectLang(long projectLangId);
}
