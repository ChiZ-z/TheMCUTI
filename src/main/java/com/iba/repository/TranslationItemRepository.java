package com.iba.repository;

import com.iba.model.glossary.TranslationItem;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface TranslationItemRepository extends JpaRepository<TranslationItem, Long> {


    @Query("select count(translationItem) from TranslationItem translationItem where translationItem.groupItemId in " +
            "(select groupItem.id from GroupItem groupItem where groupItem.glossaryId = :glossaryId) ")
    int countAllByGlossaryId(long glossaryId);

    @Query("select count(translationItem) from TranslationItem translationItem where translationItem.groupItemId in " +
            "(select groupItem.id from GroupItem groupItem where groupItem.glossaryId in " +
            "(select glossary.id from Glossary glossary where glossary.author = :user)) ")
    int countAllByUser(User user);

    @Query("select count(translationItem) from TranslationItem translationItem where translationItem.groupItemId in " +
            "(select groupItem.id from GroupItem groupItem where groupItem.glossaryId in " +
            "(select glossary.id from Glossary glossary where glossary.id in " +
            "(select follower.glossaryId from Follower follower where follower.user = :user))) ")
    int countAllByFollower(User user);

    @Query("select count(translationItem) from TranslationItem translationItem where translationItem.groupItemId in " +
            "(select groupItem.id from GroupItem groupItem where groupItem.glossaryId in " +
            "(select glossary.id from Glossary glossary where glossary.glossaryType = :glossaryType)) ")
    int countAllByGlossaryType(Constants.GlossaryType glossaryType);
}
