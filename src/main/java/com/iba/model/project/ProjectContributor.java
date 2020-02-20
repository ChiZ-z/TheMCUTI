package com.iba.model.project;


import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.Constants;
import com.iba.model.view.View;
import com.iba.model.user.User;

import javax.persistence.*;

@Entity
@Table(name = "project_contributor")
@JsonView(View.ProjectItem.class)
public class ProjectContributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User contributor;
    private Long projectId;

    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private Constants.ContributorRole role;

    @Transient
    private String projectName;
    @Transient
    private boolean selected;

    public ProjectContributor() {
    }

    public ProjectContributor(User contributor, Long projectId, Constants.ContributorRole role) {
        this.contributor = contributor;
        this.projectId = projectId;
        this.role = role;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getContributor() {
        return contributor;
    }

    public void setContributor(User contributor) {
        this.contributor = contributor;
    }

    public Constants.ContributorRole getRole() {
        return role;
    }

    public void setRole(Constants.ContributorRole role) {
        this.role = role;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
