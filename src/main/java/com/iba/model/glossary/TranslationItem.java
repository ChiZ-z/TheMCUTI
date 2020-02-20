package com.iba.model.glossary;

import com.iba.model.project.Lang;

import javax.persistence.*;

@Entity
@Table(name = "translation_item")
public class TranslationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupItemId;

    private String translationItemValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    public TranslationItem() {
    }

    public Long getGroupItemId() {
        return groupItemId;
    }

    public void setGroupItemId(Long groupItemId) {
        this.groupItemId = groupItemId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTranslationItemValue() {
        return translationItemValue;
    }

    public void setTranslationItemValue(String translationItemValue) {
        this.translationItemValue = translationItemValue;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }


}
