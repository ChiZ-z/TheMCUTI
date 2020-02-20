package com.iba.repository;

import com.iba.model.glossary.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findById(long id);

    @Query("select category from Category category where category.glossaryId = :glossaryId")
    List<Category> findAllByGlossary(long glossaryId);

    @Query("select case when count(category) > 0 then true else false end from Category category where category.glossaryId = :glossaryId and category.categoryName = :categoryName")
    boolean existsByGlossaryIdAndCategoryName(long glossaryId, String categoryName);

    @Query("select category from Category category where category.id in :glossaryIds")
    List<Category> findAllByIds(List<Long> glossaryIds);
}
