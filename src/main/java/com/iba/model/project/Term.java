package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.View;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "term")
@JsonView(View.ProjectItem.class)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private Long id;
    private Long projectId;
    
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private String termValue;

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private boolean isDeleted;

    @Transient
    private Long translatedCount;
    @Transient
    private List<TermLang> translations = new ArrayList<>();
    @Transient
    private List<TermComment> comments = new ArrayList<>();
    @Transient
    private boolean selected = false;
    @Transient
    private long commentsCount;
    @Transient
    private String referenceValue;

    public Term() {
    }

    public Term(Long projectId, String termValue) {
        this.projectId = projectId;
        this.termValue = termValue;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public Long getTranslatedCount() {
        return translatedCount;
    }

    public void setTranslatedCount(Long translatedCount) {
        this.translatedCount = translatedCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTermValue() {
        return termValue;
    }

    public void setTermValue(String termValue) {
        this.termValue = termValue;
    }

    public List<TermLang> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TermLang> translations) {
        this.translations = translations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return Objects.equals(id, term.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<TermComment> getComments() {
        return comments;
    }

    public void setComments(List<TermComment> comments) {
        this.comments = comments;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }
}
