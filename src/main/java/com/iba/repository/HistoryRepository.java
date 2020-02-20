package com.iba.repository;

import com.iba.model.history.History;
import com.iba.model.view.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    /**
     * Count stats
     * by stats.user_id && stats.action && stats.project_id && stats.contributor.
     *
     * @param id        - id of user
     * @param action    - action
     * @param projectId - id of project
     * @return number of stats
     */
    long countAllByUserIdAndActionAndProjectId(Long id, Constants.StatType action, Long projectId);

    /**
     * Count list of stats
     * by stats.user_id && stats.action &&  stats.date.
     *
     * @param id     - id of user
     * @param action - action
     * @param start  - date start from
     * @param stop   - date to end
     * @return list of stats
     */
    @Query("select count(v) from History as v where v.user.id = :userId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    @Transactional
    List<Long> countAllByUserIdAndActionAndDateBetween(Long userId, Constants.StatType action, Date start, Date stop);

    @Query("select count(v) from History as v where v.project.id = :projectId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    @Transactional
    List<Long> countAllByProjectIdAndActionAndDateBetween(Long projectId, Constants.StatType action, Date start, Date stop);

    @Query("select count(v) from History as v where v.user.id = :userId and v.project.id = :projectId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    @Transactional
    List<Long> countAllByUserIdAndProjectIdAndActionAndDateBetween(Long userId, Long projectId, Constants.StatType action, Date start, Date stop);

    /**
     * Count list of date
     * by stats.user_id && stats.action && stats.date.
     *
     * @param id     - user id
     * @param action - type of action
     * @param start  - date start from
     * @param stop   - date to end
     * @return list of date
     */
    @Query("select date(v.date) from History as v where v.user.id = :userId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    List<Date> findByUserIdAndActionAndDateBetween(Long userId, Constants.StatType action, Date start, Date stop);

    @Query("select date(v.date) from History as v where v.project.id = :projectId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    List<Date> findByProjectIdAndActionAndDateBetween(Long projectId, Constants.StatType action, Date start, Date stop);

    @Query("select date(v.date) from History as v where v.user.id = :userId and v.project.id = :projectId and v.action = :action and v.date between :start and :stop group by cast(v.date as date) ")
    List<Date> findByUserIdAndProjectIdAndActionAndDateBetween(Long userId, Long projectId, Constants.StatType action, Date start, Date stop);

    /**
     * Count list of stats
     * by stats.user_id && stats.date.
     *
     * @param id    - id of user
     * @param start - date start from
     * @param stop  - date to end
     * @return list of stats
     */
    @Query("select count(v) from History as v where v.user.id = :userId and v.action in :constantActions and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    @Transactional
    List<Long> countAllByUserIdAndDateBetween(Long userId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select count(v) from History as v where v.project.id = :projectId and v.action in :constantActions and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    @Transactional
    List<Long> countAllByProjectIdAndDateBetween(Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select count(v) from History as v where v.user.id = :userId and v.project.id = :projectId and v.action in :constantActions and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    @Transactional
    List<Long> countAllByUserIdAndProjectIdAndDateBetween(Long userId, Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    /**
     * Count list of date
     * by stats.user_id && stats.date.
     *
     * @param id    - user id
     * @param start - date start from
     * @param stop  - date to end
     * @return list of date
     */
    @Query("select date(v.date) from History as v where v.user.id = :userId and v.action in :constantActions and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    List<Date> findByUserIdAndDateBetween(Long userId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select date(v.date) from History as v where v.project.id = :projectId and v.action in :constantActions and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    List<Date> findByProjectIdAndDateBetween(Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select date(v.date) from History as v where v.user.id = :userId and v.action in :constantActions and v.project.id = :projectId and v.date between :start and :stop group by cast(v.date as date) order by cast(v.date as date) asc")
    List<Date> findByUserIdAndProjectIdAndDateBetween(Long userId, Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    /**
     * Count list of stats
     * by stats.user_id && stats.action.
     *
     * @param id   - id of user
     * @param type - action
     * @return number of stats
     */
    @Query("select count(v) from History v where v.user.id = :id and v.action = :type group by cast(v.date as date)")
    List<Long> countByUserIdAndAction(Long id, Constants.StatType type);

    @Query("select count(v) from History v where v.project.id = :id and v.action = :type group by cast(v.date as date)")
    List<Long> countByProjectIdAndAction(Long id, Constants.StatType type);

    /*@Query("select v from History v where v.project.id =:projectId and v.parentId is null and v.isDeleted = false " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectId(Long projectId, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id =:userId and v.parentId is null and v.isDeleted = false " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByUserId(Long userId, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.project.id =:projectId and v.action in :actions and v.parentId is null and v.isDeleted = false " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndProjectId(List<Constants.StatType> actions, Long projectId, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id =:userId and v.action in :actions and v.parentId is null and v.isDeleted = false " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndUserId(List<Constants.StatType> actions, Long userId, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id = :userId and v.project.id =:projectId and v.parentId is null and v.isDeleted = false " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectIdAndUserId(Long projectId, Long userId, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id = :userId and v.project.id =:projectId and v.parentId is null and v.isDeleted = false " +
            "and v.action in :actions  and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndProjectIdAndUserId(List<Constants.StatType> actions, Long projectId, Long userId, List<Constants.StatType> constantActions);*/


    @Query("select v from History v where v.project.id =:projectId and v.parentId is null and v.isDeleted = false and v.date between :start and :stop " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectIdAndDateBetween(Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.project.id =:projectId and v.parentId is null and v.isDeleted = false and v.action in :actions " +
            "and v.date between :start and :stop and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndProjectIdAndDateBetween(List<Constants.StatType> actions, Long projectId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id =:userId and v.parentId is null and v.isDeleted = false and v.date between :start and :stop " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByUserIdAndDateBetween(Long userId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id =:userId and v.parentId is null and v.isDeleted = false and v.action in :actions " +
            "and v.date between :start and :stop and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndUserIdAndDateBetween(List<Constants.StatType> actions, Long userId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id = :userId and v.project.id =:projectId and v.parentId is null and v.isDeleted = false " +
            "and v.date between :start and :stop and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectIdAndUserIdAndDateBetween(Long projectId, Long userId, Date start, Date stop, List<Constants.StatType> constantActions);

    @Query("select v from History v where v.user.id = :userId and v.project.id =:projectId and v.parentId is null and v.isDeleted = false " +
            "and v.action in :actions and v.date between :start and :stop and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByActionAndProjectIdAndUserIdAndDateBetween(List<Constants.StatType> actions, Long projectId, Long userId, Date start, Date stop, List<Constants.StatType> constantActions);


    @Query("select v from History v where v.project.id = :projectId and v.isDeleted = false and v.parentId = :parentId " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectIdAndParentId(Long projectId, Long parentId, List<Constants.StatType> constantActions);

    @Query(value = "select v from History v where v.project.id = :projectId and v.isDeleted = false and v.termLang.id = :termLangId " +
            "and v.id not in (select id from History where action in :constantActions and termLang.id is null) order by v.date desc")
    List<History> findAllByProjectIdAndTermLangId(Long projectId, Long termLangId, List<Constants.StatType> constantActions);

    /*@Transactional
    @Modifying
    @Query(value = "delete from History history where history.termLang.id in (select termLang.id from TermLang termLang where termLang.term.id = :termId)")
    void deleteHistoryByTerm(Long termId);*/

    /*@Query("select v from History v where v.project.id =:projectId and v.action not in :actions")
    List<History> findAllByProjectAndActionNotIn(Long projectId, List<Constants.StatType> actions);*/
}
