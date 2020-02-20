package com.iba.model.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.project.Term;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.View;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "history")
@JsonView(View.HistoryItem.class)
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contributor_id")
    private User contributor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_lang_id")
    private TermLang termLang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_lang_id")
    private ProjectLang projectLang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id")
    private Term term;

    @JoinColumn(name = "parent_id")
    private Long parentId;

    @JoinColumn(name = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+03:00")
    private Timestamp date;

    private boolean isDeleted;

    private String currentValue;
    private String newValue;
    private String refValue;

    @Transient
    private boolean isDisabled;

    @Enumerated(EnumType.STRING)
    private Constants.StatType action;

    public History() {
    }

    public History(User user, Project project, Constants.StatType action) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
    }

    public History(User user, Project project, Constants.StatType action, Term term) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.term = term;
    }

    public History(User user, Project project, User contributor, Constants.StatType action) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.contributor = contributor;
    }

    public History(User user, Project project, Constants.StatType action, ProjectLang projectLang) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.projectLang = projectLang;
    }

    public History(User user, Project project, Constants.StatType action, String currentValue,Term term) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.currentValue = currentValue;
    }

    public History(User user, Project project, Constants.StatType action,Term term, String currentValue, String newValue, Long parentId) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.currentValue = currentValue;
        this.newValue = newValue;
        this.parentId = parentId;
    }

    public History(User user, Project project, Constants.StatType action, TermLang termLang, ProjectLang projectLang, String currentValue, String newValue) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.currentValue = currentValue;
        this.newValue = newValue;
        this.term = termLang.getTerm();
        this.termLang = termLang;
        this.projectLang = projectLang;
    }

    public History(User user, Project project, Constants.StatType action, TermLang termLang, ProjectLang projectLang, Long parentId, String currentValue, String newValue) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.currentValue = currentValue;
        this.newValue = newValue;
        this.term = termLang.getTerm();
        this.termLang = termLang;
        this.projectLang = projectLang;
        this.parentId = parentId;
    }

    public History(User user, Project project, Constants.StatType action, TermLang termLang, ProjectLang projectLang, String currentValue, String newValue, String refValue) {
        this.user = user;
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.project = project;
        this.action = action;
        this.term = termLang.getTerm();
        this.termLang = termLang;
        this.projectLang = projectLang;
        this.currentValue = currentValue;
        this.newValue = newValue;
        this.refValue = refValue;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public ProjectLang getProjectLang() {
        return projectLang;
    }

    public void setProjectLang(ProjectLang projectLang) {
        this.projectLang = projectLang;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getContributor() {
        return contributor;
    }

    public void setContributor(User contributor) {
        this.contributor = contributor;
    }

    public TermLang getTermLang() {
        return termLang;
    }

    public void setTermLang(TermLang termLang) {
        this.termLang = termLang;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate() {
        this.date = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getRefValue() {
        return refValue;
    }

    public void setRefValue(String refValue) {
        this.refValue = refValue;
    }

    public Constants.StatType getAction() {
        return action;
    }

    public void setAction(Constants.StatType action) {
        this.action = action;
    }
}
