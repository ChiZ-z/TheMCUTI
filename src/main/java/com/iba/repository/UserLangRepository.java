package com.iba.repository;

import com.iba.model.user.UserLang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserLangRepository extends JpaRepository<UserLang, Long> {
}
