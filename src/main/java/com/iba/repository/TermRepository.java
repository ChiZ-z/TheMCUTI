package com.iba.repository;

import com.iba.model.project.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    @Query("select case " +
            "when term.isDeleted = true then true " +
            "when count(term.id) = 0 then true " +
            "when (select project.isDeleted from Project project where project.id = term.projectId) = true then true " +
            "when (select count(project.id) from Project project where project.id = term.projectId) = 0 then true " +
            "else false end from Term term where term.id = :termId")
    boolean existsByTermIsDeletedAndProjectIsDeleted(long termId);

    /**
     * Find Term
     * by Term.id.
     *
     * @param id termId
     * @return Term with this id
     */
    @Query("select term from Term term where term.id = :termId and term.isDeleted = false ")
    Term findById(long termId);

    List<Term> findByProjectIdAndIsDeletedFalse(Long projectId);


    /**
     * Count all Terms
     * by Term.projectId.
     *
     * @param id id of project
     * @return number of terms in project
     */
    @Query("select count(term) from Term term where term.projectId = :projectId and term.isDeleted = false ")
    long countAllByProjectId(long projectId);

    /**
     * Count all Terms
     * owned by userId
     *
     * @param userId userId
     * @return number of terms in projects owned by userId
     */
    @Query(value = "select count(term) from Term term where term.projectId in " +
            "(select project.id from Project project where project.author.id = :userId and project.isDeleted = false or project.id in " +
            "(select projectContributor.projectId from ProjectContributor projectContributor where projectContributor.contributor.id = :userId and projectContributor.isDeleted = false )) " +
            "and term.isDeleted = false ")
    long countAllTermsByUserId(long userId);

    /**
     * Check exist Term in project with this value.
     *
     * @param projectId projectId
     * @param termValue value of Term
     * @return true if term exist in project
     * and value of term is equal termValue
     */
    @Query("select case when count(term) > 0 then true else false end from Term term where term.isDeleted = false and term.projectId = :projectId and term.termValue = :termValue")
    boolean existsByProjectIdAndTermValue(@Param("projectId") Long projectId, @Param("termValue") String termValue);

    @Modifying
    @Query("update Term t set t.isDeleted = true where t = :term")
    @Override
    void delete(Term term);

    @Modifying
    @Query("update Term t set t.isDeleted = true where t in :terms")
//    @Override
    void deleteAll(List<Term> terms);

    @Query("select term from Term term where term.id in :terms")
    List<Term> selectTerms(List<Long> terms);
}
