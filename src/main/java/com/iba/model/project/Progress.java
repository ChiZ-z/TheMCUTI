package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.Constants;
import com.iba.model.view.View;

import javax.persistence.Transient;

@JsonView(View.ProjectItem.class)
public class Progress {
    @Transient
    private long termsCount;
    @Transient
    private long translationsCount;
    @Transient
    private long contributorsCount;
    @Transient
    private long projectsCount;
    @Transient
    private double progress;
    @Transient
    private String projectName;
    @Transient
    private String languageName;
    @Transient
    private String description;
    @Transient
    private Constants.ContributorRole role;
    @Transient
    private String languageDefinition;

    public Progress() {
    }

    public Progress(long termsCount, long translatedCount, long projectsCount, double progress) {
        this.termsCount = termsCount;
        this.translationsCount = translatedCount;
        this.projectsCount = projectsCount;
        this.progress = progress;
    }

    public Progress(long termsCount, long translatedCount, long allTerms, long contributorsCount, String projectName, String description, Constants.ContributorRole role) {
        this.termsCount = termsCount;
        this.translationsCount = translatedCount;
        this.contributorsCount = contributorsCount;
        this.projectName = projectName;
        this.description = description;
        this.role = role;
        if (translationsCount > 0 && allTerms > 0) {
            this.progress = (double) translationsCount / (double) allTerms;
        } else this.progress = 0.0;
    }

    public Progress(long termsCount, long translationsCount, double progress, String projectName, String languageName, String description, String languageDefinition) {
        this.termsCount = termsCount;
        this.translationsCount = translationsCount;
        this.progress = progress;
        this.description = description;
        this.languageName = languageName;
        this.projectName = projectName;
        this.languageDefinition = languageDefinition;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public Constants.ContributorRole getRole() {
        return role;
    }

    public void setRole(Constants.ContributorRole role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(long projectsCount) {
        this.projectsCount = projectsCount;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(long termsCount) {
        this.termsCount = termsCount;
    }

    public long getTranslationsCount() {
        return translationsCount;
    }

    public void setTranslationsCount(long translationsCount) {
        this.translationsCount = translationsCount;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public long getContributorsCount() {
        return contributorsCount;
    }

    public void setContributorsCount(long contributorsCount) {
        this.contributorsCount = contributorsCount;
    }

    public String getLanguageDefinition() {
        return languageDefinition;
    }

    public void setLanguageDefinition(String languageDefinition) {
        this.languageDefinition = languageDefinition;
    }
}
