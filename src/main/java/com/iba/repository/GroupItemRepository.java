package com.iba.repository;

import com.iba.model.glossary.GroupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupItemRepository extends JpaRepository<GroupItem, Long> {

    GroupItem findById(long id);
}
