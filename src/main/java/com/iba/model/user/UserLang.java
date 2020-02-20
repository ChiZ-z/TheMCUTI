package com.iba.model.user;

import com.iba.model.project.Lang;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_langs")
public class UserLang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    private String level;
    private Long userId;

    public UserLang() {
    }

    public UserLang(Lang lang, String level, Long userId) {
        this.lang = lang;
        this.level = level;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLang userLang = (UserLang) o;
        return Objects.equals(id, userLang.id);
    }
}
