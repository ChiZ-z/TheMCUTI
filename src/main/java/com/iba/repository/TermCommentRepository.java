package com.iba.repository;

import com.iba.model.project.TermComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TermCommentRepository extends JpaRepository<TermComment, Long> {
    /**
     * Find all comments
     * by term_comment.term_id.
     *
     * @param id - id of term
     * @return List of TermComment
     */
    @Query("select termComment from TermComment termComment where termComment.termId = :id order by termComment.id ASC")
    List<TermComment> findAllByTermId(long id);

    /**
     * Count all comments
     * by term_comment.term_id.
     *
     * @param id - id of term
     * @return count of comments
     */
    Long countAllByTermId(long id);
}
