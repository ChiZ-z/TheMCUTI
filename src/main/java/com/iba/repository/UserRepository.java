package com.iba.repository;

import com.iba.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find User
     * by user.email.
     *
     * @param email incoming email of search user
     * @return the User where the user's email equals an email to search for
     */
    @Query("select user from User user where user.email = :email")
    User findByEmail(String email);

    /**
     * Find User
     * by user.username.
     *
     * @param username incoming username of search user
     * @return the User where the user's username equals an username to search for
     */
    User findByUsername(String username);

    /**
     * Find User
     * by user.activationCode.
     *
     * @param code incoming username of search user
     * @return the User where the user's activationCode equals an activationCode to search for
     */
    User findByActivationCode(String code);

    /**
     * Count all user's with this username.
     *
     * @param username incoming username of search users
     * @return count of users with this username
     */
    int countAllByUsername(String username);

    /**
     * Count all user's with this email.
     *
     * @param email incoming email of search users
     * @return count of users with this email
     */
    int countAllByEmail(String email);

    /**
     * Find Users
     * by user.username, by user.FirstName, by user.lastName
     *
     * @param searchValue incoming searchValue
     * @return list of users where the user's username or firstName or lastName contains searchValue
     */
    @Query("select o from User o where LOWER(o.username) like CONCAT('%',SUBSTRING_INDEX(:searchValue, ' ', 1),'%') " +
            "or LOWER(o.firstName) like CONCAT('%',SUBSTRING_INDEX(:searchValue, ' ', 1),'%') " +
            "or LOWER(o.lastName) like CONCAT('%',SUBSTRING_INDEX(:searchValue, ' ', 1),'%')" +
            "or LOWER(o.firstName) like CONCAT('%',SUBSTRING_INDEX(:searchValue, ' ', -1),'%')" +
            "or LOWER(o.lastName) like CONCAT('%',SUBSTRING_INDEX(:searchValue, ' ', -1),'%')")
    List<User> findByFirstNameAndLastNameAndUsername(String searchValue);

    /**
     * Find User
     * by user.id.
     *
     * @param id incoming id of search user
     * @return the User with id for search
     */
    User findById(long id);

    @Query("select user from User user where user.email = :email and user.firstName = :firstName ")
    User findByFirstNameAndEmail(String firstName, String email);

    @Query("select projContr.contributor from ProjectContributor projContr where projContr.projectId = :projectId and projContr.isDeleted = false")
    List<User> findAllByProjectId(Long projectId);
}
