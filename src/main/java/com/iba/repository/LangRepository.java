package com.iba.repository;

import com.iba.model.project.Lang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LangRepository extends JpaRepository<Lang, Long> {

    /**
     * Find Lang
     * by lang.id
     *
     * @param id incoming id for search
     * @return Lang where lang id equal search id
     */
    Lang findById(long id);

    /**
     * @return all langs order by ASC
     */
    @Query("select o from Lang o order by o.langName ASC")
    List<Lang> findAll();

    /**
     * Find List of Langs
     * by user_langs.lang_id.
     *
     * @param userId id of user
     * @return list of langs that user set
     */
    @Query(value = "select o.lang from UserLang o where o.userId = ?1")
    List<Lang> findByUserId(Long userId);

    Lang findByLangName(String langName);

    Lang findByLangDef(String langDef);
}
