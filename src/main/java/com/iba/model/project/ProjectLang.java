package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_lang")
@JsonView(View.ProjectItem.class)
public class ProjectLang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private Long id;
    private Long projectId;

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    @OneToMany(mappedBy = "projectLangId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Where(clause = "term_id in (select term.id from term where term.is_deleted = false)")
    private List<TermLang> termLangs = new ArrayList<>();

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private boolean isDeleted;
    private boolean isDefault;

    @Transient
    private Constants.ContributorRole role;
    @Transient
    private long termsCount;
    @Transient
    private long translatedCount;
    @Transient
    private PageParams pageParams;
    @Transient
    private long countChangeDefault;
    @Transient
    private long countFuzzy;
    @Transient
    private long countAutotranslated;

    public ProjectLang() {
    }

    public ProjectLang(Lang lang, boolean isDefault) {
        this.lang = lang;
        this.isDefault = isDefault;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCountChangeDefault() {
        return countChangeDefault;
    }

    public void setCountChangeDefault(long countChangeDefault) {
        this.countChangeDefault = countChangeDefault;
    }

    public long getCountFuzzy() {
        return countFuzzy;
    }

    public void setCountFuzzy(long countFuzzy) {
        this.countFuzzy = countFuzzy;
    }

    public long getCountAutotranslated() {
        return countAutotranslated;
    }

    public void setCountAutotranslated(long countAutotranslated) {
        this.countAutotranslated = countAutotranslated;
    }

    public Constants.ContributorRole getRole() {
        return role;
    }

    public void setRole(Constants.ContributorRole role) {
        this.role = role;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
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

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public List<TermLang> getTermLangs() {
        return termLangs;
    }

    public void setTermLangs(List<TermLang> termLangs) {
        this.termLangs = termLangs;
    }

    public long getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(long termsCount) {
        this.termsCount = termsCount;
    }

    public long getTranslatedCount() {
        return translatedCount;
    }

    public void setTranslatedCount(long translatedCount) {
        this.translatedCount = translatedCount;
    }
}
