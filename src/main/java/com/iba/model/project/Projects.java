package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;

import java.util.List;

@JsonView(View.ProjectItem.class)
public class Projects {

    private List<Project> projectList;
    private PageParams pageParams;
    private Progress progress;

    public Projects() {
    }

    public Projects(List<Project> projectList, PageParams pageParams, Progress progress) {
        this.projectList = projectList;
        this.pageParams = pageParams;
        this.progress = progress;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }
}
