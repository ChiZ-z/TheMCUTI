package com.iba.model.glossary;

import com.iba.model.project.Lang;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_item")
public class GroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;

    private Long glossaryId;

    @OneToMany(mappedBy = "groupItemId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<TranslationItem> translationItems = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "group_item_category",
            joinColumns = @JoinColumn(name = "group_item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = new ArrayList<>();

    @Transient
    private TranslationItem defaultTranslationItem;
    @Transient
    private boolean defaultEmpty;

    public GroupItem() {
    }

    public GroupItem(Long glossaryId, List<TranslationItem> translationItems, List<Category> categories) {
        this.glossaryId = glossaryId;
        this.translationItems = translationItems;
        this.categories = categories;
    }

    public TranslationItem getDefaultTranslationItem() {
        return defaultTranslationItem;
    }

    public void setDefaultTranslationItem(TranslationItem defaultTranslationItem) {
        this.defaultTranslationItem = defaultTranslationItem;
    }

    public boolean isDefaultEmpty() {
        return defaultEmpty;
    }

    public void setDefaultEmpty(boolean defaultEmpty) {
        this.defaultEmpty = defaultEmpty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getGlossaryId() {
        return glossaryId;
    }

    public void setGlossaryId(Long glossaryId) {
        this.glossaryId = glossaryId;
    }

    public List<TranslationItem> getTranslationItems() {
        return translationItems;
    }

    public void setTranslationItems(List<TranslationItem> translationItems) {
        this.translationItems = translationItems;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
