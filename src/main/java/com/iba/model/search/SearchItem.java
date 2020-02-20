package com.iba.model.search;

import com.iba.model.view.Constants;

public class SearchItem {
    private Constants.SearchListType category;
    private String projectName;
    private Long translatedCount;
    private Long translatedAll;
    private String translation;
    private String term;
    private String lang;
    private String langIcon;
    private String searchedValue;
    private Long projectId;
    private Long projectLangId;

    public SearchItem() {
    }

    public SearchItem(Constants.SearchListType category, String projectName, String term, String searchedValue, Long projectId) {
        this.category = category;
        this.projectName = projectName;
        this.term = term;
        this.searchedValue = searchedValue;
        this.projectId = projectId;
    }

    public Constants.SearchListType getCategory() {
        return category;
    }

    public String getLangIcon() {
        return langIcon;
    }

    public void setLangIcon(String langIcon) {
        this.langIcon = langIcon;
    }

    public void setCategory(Constants.SearchListType category) {
        this.category = category;
    }

    public Long getTranslatedCount() {
        return translatedCount;
    }

    public void setTranslatedCount(Long translatedCount) {
        this.translatedCount = translatedCount;
    }

    public Long getTranslatedAll() {
        return translatedAll;
    }

    public void setTranslatedAll(Long translatedAll) {
        this.translatedAll = translatedAll;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSearchedValue() {
        return searchedValue;
    }

    public void setSearchedValue(String searchedValue) {
        this.searchedValue = searchedValue;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getProjectLangId() {
        return projectLangId;
    }

    public void setProjectLangId(Long projectLangId) {
        this.projectLangId = projectLangId;
    }
}
