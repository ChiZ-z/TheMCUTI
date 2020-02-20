package com.iba.repository;

import com.iba.model.glossary.Follower;
import com.iba.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowersRepository extends JpaRepository<Follower, Long> {

    Follower findByActivationCode(String activationCode);

    List<Follower> findByUser(User user);

    Follower findByUserAndGlossaryId(User user,long glossaryId);

    @Query("select case " +
            "when count(follower.id) > 0 then true " +
            "else false end from Follower follower where follower.glossaryId = :glossaryId " +
            "and follower.user.id = :userId and follower.role = 'MODERATOR' and follower.activationCode is null")
    boolean isModerator(long glossaryId, long userId);
}
