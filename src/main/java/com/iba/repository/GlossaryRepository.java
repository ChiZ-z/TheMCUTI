package com.iba.repository;

import com.iba.model.glossary.Glossary;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GlossaryRepository extends JpaRepository<Glossary, Long> {

    Glossary findById(long id);

    @Query("select count(glossary) from Glossary glossary  where glossary.author = :user ")
    int countByAuthor(User user);

    @Query("select count(glossary) from Glossary glossary  where glossary.id in " +
            "(select follower.glossaryId from Follower follower where follower.user = :user) ")
    int countByFollower(User user);

    List<Glossary> findByAuthor(User user);

    @Query("select glossary from Glossary glossary where glossary.id in " +
            "(select follower.glossaryId from Follower follower where follower.user = :user and follower.activationCode is null)")
    List<Glossary> findByFollower(User user);

    int countByGlossaryType(Constants.GlossaryType glossaryType);

    @Query("select glossary from Glossary glossary where glossary.glossaryType in :glossaryTypes")
    List<Glossary> findByGlossaryTypes(List<Constants.GlossaryType> glossaryTypes);

}
