package com.iba.model.chart;


public class ResultStat {
    private long translateCount;
    private long editCount;
    private long autoTranslateCount;
    private long projectsCount;
    private long translateByImportCount;
    private long editByImportCount;
    private double rating;

    public ResultStat() {
    }

    public long getTranslateCount() {
        return translateCount;
    }

    public void setTranslateCount(long translateCount) {
        this.translateCount = translateCount;
    }

    public long getEditCount() {
        return editCount;
    }

    public void setEditCount(long editCount) {
        this.editCount = editCount;
    }

    public long getAutoTranslateCount() {
        return autoTranslateCount;
    }

    public void setAutoTranslateCount(long autoTranslateCount) {
        this.autoTranslateCount = autoTranslateCount;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(long projectsCount) {
        this.projectsCount = projectsCount;
    }

    public long getTranslateByImportCount() {
        return translateByImportCount;
    }

    public void setTranslateByImportCount(long translateByImportCount) {
        this.translateByImportCount = translateByImportCount;
    }

    public long getEditByImportCount() {
        return editByImportCount;
    }

    public void setEditByImportCount(long editByImportCount) {
        this.editByImportCount = editByImportCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
