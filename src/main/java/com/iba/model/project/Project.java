package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;
import com.iba.model.user.User;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "project")
@JsonView(View.ProjectItem.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private Long id;

    @JsonView(View.ProjectItem.class)
    private Timestamp lastUpdate;

    @JsonView(View.ProjectItem.class)
    private Timestamp creationDate;

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private String projectName;
    private String description;

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Where(clause = "is_deleted = false")
    private List<Term> terms = new ArrayList<>();

    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    @OneToMany(mappedBy = "projectId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Where(clause = "is_deleted = false")
    private List<ProjectLang> projectLangs = new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Where(clause = "is_deleted = false")
    private List<ProjectContributor> contributors = new ArrayList<>();

    @Transient
    private PageParams pageParams;
    @Transient
    private long termsCount;
    @Transient
    private double progress;
    @Transient
    private Constants.ContributorRole role;

    public Project() {
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate() {
        this.lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());
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

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<ProjectContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<ProjectContributor> contributors) {
        this.contributors = contributors;
    }

    public List<ProjectLang> getProjectLangs() {
        return projectLangs;
    }

    public void setProjectLangs(List<ProjectLang> projectLangs) {
        this.projectLangs = projectLangs;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public long getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(long termsCount) {
        this.termsCount = termsCount;
    }

}
