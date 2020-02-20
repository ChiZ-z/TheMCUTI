package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.View;

import javax.persistence.*;

@Entity
@Table(name = "lang")
@JsonView({View.ProjectItem.class, View.HistoryItem.class})
public class Lang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String langName;
    private String langDef;
    private String langIcon;

    public Lang() {
    }

    public Lang(String langName) {
        this.langName = langName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLangName() {
        return langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public String getLangDef() {
        return langDef;
    }

    public void setLangDef(String langDef) {
        this.langDef = langDef;
    }

    public String getLangIcon() {
        return langIcon;
    }

    public void setLangIcon(String langIcon) {
        this.langIcon = langIcon;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Lang lang = (Lang) object;
        return langName.equals(lang.langName) &&
                langDef.equals(lang.langDef) &&
                id.equals(lang.id);
    }
}
